package com.games.towerdefense;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.games.towerdefense.enums.DamageType;
import com.games.towerdefense.enums.GUIState;
import com.games.towerdefense.enums.GameState;
import com.games.towerdefense.enums.MetaGameState;
import com.games.towerdefense.enums.MovementDirection;
import com.games.towerdefense.enums.TowerType;
import com.games.towerdefense.skills.*;

public class GameManager
{
	private final int				INVALID_POINTER	= -1;
	private final int				INVALID_TILE	= -1;
	private final int				INVALID_TOWER	= -1;
	
	private static GameView			gameView;
	
	private Random					random			= new Random();
	
	private MetaGameState			metaGameState	= MetaGameState.Game;
	private GameState				gameState		= GameState.WaveNotStarted;
	
	private float					cameraX			= 0;
	private float					cameraY			= 0;
	
	private int						pointerInUseID	= INVALID_POINTER;
	private float					pointerX;
	private float					pointerY;

	private Boolean					didClick		= false;
	private int						selectedTileX	= INVALID_TILE;
	private int						selectedTileY	= INVALID_TILE;
	private int						prevTileX		= INVALID_TILE;
	private int						prevTileY		= INVALID_TILE;
	
	private static float			scale;
	private Paint					paint			= new Paint();
	private Typeface				font;
	
	private GUIState				guiState		= GUIState.None;
	private Bitmap					buttonSelect;
	private Bitmap					buttonCancel;
	private Bitmap					buttonStart;
	
	private Map						map;
	
	private Tower[]					userTowers		= new Tower[8];
	private int						towerToBuy;
	
	private ArrayList<Tower>		towerList		= new ArrayList<Tower>();
	private int						selectedTower	= INVALID_TOWER;
	
	private ArrayList<EnemyWave>	enemyWaveList	= new ArrayList<EnemyWave>();
	private int						currentWave;
	
	private int						money			= 100;
	private int						lives			= 10;
	
	public GameManager(Context context, GameView gameView)
	{
		GameManager.gameView = gameView;
		
		int[][][] groundLayers =
		{
			{
				{  0, 26,  0,  0,  0, 26, 26, 26, 13, 13, 13 },
				{  0, 26,  0,  0,  0, 26,  0, 26,  0,  0,  0 },
				{  0, 26,  0,  0,  0, 26,  0, 26, 26, 26, 26 },
				{  0, 26, 26, 26,  0, 26,  0,  0,  0,  0,  0 },
				{  0,  0,  0, 26,  0, 26,  0, 26, 26, 26,  0 },
				{  0, 26, 26, 26,  0, 26,  0, 26,  0, 26, 26 },
				{  0, 26,  0,  0,  0, 26, 26, 26,  0,  0, 26 },
				{ 26, 26,  0,  0, 13,  0,  0,  0,  0, 26, 26 },
				{ 26,  0,  0,  0,  0,  0,  0,  0,  0, 26,  0 },
				{ 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,  0 }
			},
			{
				{ -1, 20, -1, -1, -1, 43, 19, 44, 57, 19, 58 },
				{ -1, 20, -1, -1, -1, 20, -1, 20, -1, -1, -1 },
				{ -1, 20, -1, -1, -1, 20, -1, 55, 19, 19, 19 },
				{ -1, 55, 19, 44, -1, 20, -1, -1, -1, -1, -1 },
				{ -1, -1, -1, 20, -1, 20, -1, 43, 19, 44, -1 },
				{ -1, 43, 19, 56, -1, 20, -1, 20, -1, 55, 44 },
				{ -1, 20, -1, -1, -1, 55, 19, 56, -1, -1, 20 },
				{ 43, 56, -1, -1, 16, -1, -1, -1, -1, 43, 56 },
				{ 20, -1, -1, -1, -1, -1, -1, -1, -1, 20, -1 },
				{ 55, 19, 19, 19, 19, 19, 19, 19, 19, 56, -1 }
			},
			{
				{ -1, -1, -1, -1, -1, 41, -1, 42, -1, -1, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, 53, -1, -1, -1 },
				{ -1, 53, -1, 42, -1, -1, -1, -1, -1, -1, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, 41, -1, 42, -1 },
				{ -1, 41, -1, 54, -1, -1, -1, -1, -1, 53, 42 },
				{ -1, -1, -1, -1, -1, 53, -1, 54, -1, -1, -1 },
				{ 41, 54, -1, -1, -1, -1, -1, -1, -1, 41, 54 },
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
				{ 53, -1, -1, -1, -1, -1, -1, -1, -1, 54, -1 }
			}
		};
		int[][] collisionLayer =
			{
				{ -1,  0, -1, -1, -1,  0,  0,  0, 99, 99, 99 }, // 0 = ROAD
				{ -1,  0, -1, -1, -1,  0, -1,  0, -1, -1, -1 }, // 42 = TOWER
				{ -1,  0, -1, -1, -1,  0, -1,  0,  0,  0,  0 }, // 99 = MUU
				{ -1,  0,  0,  0, -1,  0, -1, -1, -1, -1, -1 },
				{ -1, -1, -1,  0, -1,  0, -1,  0,  0,  0, -1 },
				{ -1,  0,  0,  0, -1,  0, -1,  0, -1,  0,  0 },
				{ -1,  0, -1, -1, -1,  0,  0,  0, -1, -1,  0 },
				{  0,  0, -1, -1, 99, -1, -1, -1, -1,  0,  0 },
				{  0, -1, -1, -1, -1, -1, -1, -1, -1,  0, -1 },
				{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1 }
			};
		
		
		//HUOM!!! HARDCODED WIDTH & HEIGHT
		float scaleToFitX = Helper.Ceil(800.0f / groundLayers[0][0].length / 32.0f, 3); //[0][0], koska ei viel� k��nnetty karttaa
		float scaleToFitY = Helper.Ceil(480.0f / groundLayers[0].length / 32.0f, 3);
		if (scaleToFitX > scaleToFitY)
			scale = scaleToFitX;
		else
			scale = scaleToFitY;
		
		font = Typeface.createFromAsset(gameView.getResources().getAssets(), "source_sans_pro.otf");
		paint.setTypeface(font);
		
		buttonSelect = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.button_select);
		buttonCancel = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.button_cancel);
		buttonStart = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.button_start);
		
		map = new Map(context, R.drawable.tileset, groundLayers, collisionLayer, 1, -1, MovementDirection.Down);
		
		userTowers[0] = new Tower(TowerType.Melee, new Lunge(DamageType.Physical, 1, 2.0f, 1000), 1, 2.0f, 1000);
		userTowers[2] = new Tower(TowerType.Melee, new Whirlwind(DamageType.Physical, 1, 2.0f, 2000), 1, 2.0f, 1000);
		userTowers[6] = new Tower(TowerType.Ranged, new ProjectileSkill(DamageType.Magical, 2, 3.0f, 2000), 2, 3.0f, 2000);
		
		enemyWaveList.add(new EnemyWave(random, map, 10, 1000));
		enemyWaveList.add(new EnemyWave(random, map, 15, 2000));
		enemyWaveList.add(new EnemyWave(random, map, 18, 2000));
		enemyWaveList.add(new EnemyWave(random, map, 24, 2000));
	}
	
	public static Resources GetResources() { return gameView.getResources(); }
	public static float GetScale() { return scale; }
	
	//http://xnatd.blogspot.fi/2011/06/pathfinding-tutorial-part-1.html
	//http://world-editor-tutorials.thehelper.net/towerdef.php
	
	//800, 480
	//scale 1.5 => 48x48 => 12x10 tile� (576x480) => 224 free
	//scale 3.0 => 96x96
	
	//zoomingii 1.0 => 1.5 ja/tai 2.0 => 3.0
	//ZOOM +/- nappulat vaikkapa oikealle GUI:hin
	
	//kartan siirt�minen ja tilen valitseminen rajattu x suunnassa (GUI:n takia siis offset jeap)
	
	//HUOMIOIDA se, ett� jos vaikka k�ytt�� samaa pointteria (activePointerID) siin� oikeanpuoleisessa GUI:ssa,
	//ett� jos move map.GetPixelWidth() jommalkummal puolel toiselt puolelt nii activePointerID = -1 mieluiten.
	
	//Oikealla keskell� <| merkki, jota painamalla GUI esille. Se X offset t�llases tapaukses.
	//Toinen mahis et jostaki ku painaa, vaik oikeal ylh��l, nappia nii aukasee hover GUI. Siit� ottaa towerin nii
	//sulkee GUI:n ja voi placee sen towerin. Napit t�n placetuksen ajaks "Place" & "Cancel".
	//
	//Sit ku toi on, nii towerin valitteminen kans implement.
	//Oikealle ylh��lle nappi "Info", joka aukasee samankaltasen GUI:n kuin edellisess�.
	
	//HUOM! Jos painaa kartan ulkopuolelta, eli jos scale & kartta tarpeeks pienii, niin ei rekister�i, jos
	//painaa niit "nappei". Johtuu siit, et k�ytet�� samaa "firstPointerID" ku kartan liikkumisen kanssa
	
	//Tarviiko (Helper.) roundingii, kun cameraX/Y muutetaan? Kaikkiin kohtiin mis se muuttuu, koittaa
	//vaik 0 - 4 decimal places py�rist�� se ja kattoa jos towerit pysyy sitte liikkumisen mukana.
	
	//Vihollisen kuollessa yl�sp�in vaikka about (scaled)tilen verran nouseva teksti + kuva.
	//+"SAADUT_KOLIKOT" "PY�RIV�_KOLIKKO_ANIMAATIO". Nousee tosiaan yl�sp�in joko tietyn px m��r�n tai animaatio kertojen ajan.
	
	//Towerityypit rakennetaan itse, hinnat skillien ja/tai statsien mukaan.
	
	//Kentt�tyyppei:
	//1. Viholliset tulee ruudun ulkopuolelta ja yritt�� ulos toisesta p��st� (esim. intercept enemy troops)
	//2. Viholliset tulee ruudun ulkopuolelta ja yritt�� p��st� tiettyyn pisteeseen (esim. prevent enemies from destroying/capturing X)
	//3. Viholliset tulee tietyst� pisteest� kentt�� ja yritt�� p��st� ulos kent�st� (esim. prevent enemies from fleeing)
	//
	//=> "Story", esim.
	//Ensin 1 "Intercept enemy carriages/MIEHIST�NKULJETUSVAUNUT"
	//	Jos p��si l�pi niin jokaista kohti vaunua kohti tulee tietty m��r� vihuja seuraavassa kent�ss�.
	//		T�m�n kent�s tyyppi voi olla joko 1 tai 2. Viholllisten m��r� tosiaan verrannollinen vaunujen m��r��n.
	//		Kuitenkin, jos vaunuja v�h�n, nii voi olla enem�mpi muka niis vaunuis et v�h�n haastetta tai jtn.
	//		1 VAUNU = 1 WAVE!
	//	Jos �skeisess� oli kent�n tyyppi 2, voisi seuraava kentt� olla my�skin tyyppi� 2, mutta pelataan niin kauan,
	//		ett� damaged X (jos siis ollenkaan damaged) on korjattu. T�ll� X:ll�, jota vahingoitetaan (esim. linna),
	//		voisi olla eri damaged state kuvia, ett� n�kee paljon tuhottu/korjattu. Yll� progress bar.
	//	Toinen vaihtoehto, etteiv�t yrit� tuhota, vaan capturettaa sen X:n. T�ll�in my�s progress bar,
	//		joka n�ytt�� paljon X on vallattu.
	//	T�st� hyp�tt�isiin 3. kentt�tyyppiin, jos ne sen valtaa, paitti et wtf ei mtn j�rke�.
	//Kun kentt� vaihtuu t�mm�sess� "pelimoodissa", voisi seuraava kentt� alkaa siit�, mihin edellinen loppui.
	//	T�ll�in voisi my�s olla vaihtoanimaatio, kun painaa next stage/level, jossa ruutu liikkuu siihen suuntaan,
	//	mihin kentt� loppui, ja n�ytt�� samalla edellist� kentt�� ja seuraavaa, ett� n�kee niiden olevan connected.
	//ENIWEIS jotain t�mm�st VOIS kehitt��, mut pit�s olla kyl my�s normi pelimoodi t�n "story moden" lis�ks.
	//	Samat tower tyypit kummassaki? Dnoes, mby 2 templatee tower tyypeille, joista voi valita mit� k�ytt�� & muokkaa.
	
	//Atm nykii liike => FPS ep�tasainen (lienee isoja hyppyj�, kun liikkeen PIT�IS olla FPS-riippumatonta...)
	//http://www.coolbasic.com/phpBB3/viewtopic.php?f=12&t=298, t�� my�s talteen jos auttaa
	
	//REDUCE LAG, koittaa:
	//0. TARKASTELE T�T� http://www.jpct.net/wiki/index.php/Profiling_Android_Applications
	//		Also ottaa selv��, onko real time esim. millisekunttei
	//1. V�hent�� koko ajan uudelleen luotavia objekteja
	//2. "Profiling memory allocation" => (Jos 1. esiintyy viel)
	//		http://developer.android.com/tools/debugging/debugging-tracing.html
	//3.	http://android-coding.blogspot.ca/2012/01/flickering-problems-due-to-double.html
	//4.	http://obviam.net/index.php/the-android-game-loop/
	//		http://gamasutra.com/view/feature/171774/getting_high_precision_timing_on_.php?print=1
	//		http://www.mysecretroom.com/www/programming-and-software/android-game-loops
	//		http://gamedev.stackexchange.com/questions/1589/fixed-time-step-vs-variable-time-step
	//		http://gamedev.stackexchange.com/questions/9945/constant-game-speed-independent-of-variable-fps-in-opengl-with-glut
	//		http://stackoverflow.com/questions/3219827/how-do-i-get-a-consistent-speed-on-new-as-well-as-old-iphones
	//5. Manifestiin <Application> attribuutiks android:hardwareAccelerated="true" (ei toiminu viimeks kyl et juu)
	
	//When placing tower, se rectangle piirt�� heti tile mapin j�lkeen => vihut tulee p��lle.
	
	//TEST
	//Enemy.distanceMoved
	//Toimiiko GetWorldX/Y() => scale pienemm�ks, ett� n�kyy yli kartan (scale just sen toisen ulottuvuuden mukaan)
	//=> Yleens�ki onko se oiGein (tosiaan WORLD x/y, joo kyl ok jeap.)
	//Enemy & Projectile bounds onko oikein
	//Eri damagesia projectileit� towerissa, onko dmg oikein. Ehk� muuttaa koko systeemi erilaiseks
	//Kill countit menee oikein
	
	//Towerin attack vuoron odotus l�htee nollasta atm et juuh
	
	//Jos delete (selected) tower, niin selectedTower = INVALID_TOWER
	
	//ACTIVE ABILITY + PASSIVE ABILITY
	//=> Active voi olla normi attack
	//=> Passive voi olla tyhj�
	//=> N�iden mukaan hinta
	//=> Jos LVL/RANK UP toteuttaa niin n�m�ki vois skaalautua, ehk� tyyliin LoL.
	//==> LVL/RANK UPissa voi my�s skaalautua base statsit �skeisen lis�ksi TAI tilalta.
	
	//optimoinnin kannalta, jos alkaa t�kkim��n ja varsinkin jos profiloidessa n�ytt�� et Projetile-Enemy collisionissa menee kauan,
	//vois laittaa vaik distancen ekaks tarkistukseks, ottaa huomioon enemyn JA projectilen bitmapin koot (ja scale).
	//esmes xDiff^2 + yDiff^2 > dist^2 tai abs(xDiff) > (projWidth + enemyWidth) || abs(yDiff) > (projHeight + enemyHeight)
	//vika varmaan noppein, mut se pit�s kattoo onko valid, also v�h�n leeway kuitenki laittaa siihe
	
	//jos voi spawn kartan sis�ll�, nii enemyn logiikassa hasEnteredMap => HasGotAway() ei toimi oikein
	
/*
	ennakointi
	ottaa vihun suunta + speedpersec ja siihe suuntaan <= 1 sek verran t�h�t� esim. My�s vois koittaa ottaa tower-enemy distanssin huomioon
	
	oma vector(2) luokka, operator overloading kattella
	
	katsella miten laskea Tower.attackSpeedin N�YTETT�V� arvo
	
	didClick parempi nimi olis didClickInsideMap
	
	ei voi valita v�ltt�m�tt� OK/Cancel nappien alta tilej�
	=> Esim. game dev storyn kaltasesti ostaa torni kun painaa toisen kerran samaa tile�
	==> "Press selected tile again to buy/place tower"
	
	joku ProgressBar tjsp. luokka, jolla vois hoitaa HealthBarin ja AttackTimerin
	=> Pit�s voida valita mihin suuntaan laskee & v�riskaala tai jtn jeaps
	
	vois olla textPaint ja primitivePaint ainaki jos jompikumpi niQ quite unchanging
	
	selectedTileX/Y about joka kerta laskea touchUpissa, esim. if (didClick) j�lkeen?
	
	samat jutskat drawissa ja touchupissa koskien buy menu juttua. yhistell� luokaks / muuttujiks
	
	buy:n vois laittaa siihenki jos tower selected tai viel parempi, yhist�� GUIState None ja TowerSelected
	=> Muistaa selectedTower laittaa INVALID_TOWERiks ku guiState vaihtuu.
	
	static (final) kaik tower ja enemy bitmapit tai mieluummin ehk Tower ja Enemy classeihin niku projectile hoitaa
	=> my�s esim. scale staattiseks (finaliks?) nii ei tarvi antaa aina parametrin�
	=> kattella EnemyWaven parametrej� jos sais v�henettyy ja yleisesti koko koodi siisti�
	
	http://stackoverflow.com/questions/5878952/cast-int-to-enum-in-java
	=> ENUM.values()[index] on kallis operaatio
	
	
	WAVEN ALOITTAMINEN esim. napista. T�ll�in voisi enable towerien oston between waves
	=> UpdateTowerBuying tjsp. shittii ettei tarvi useasti samaa kirjottaa
	==> Samaa kattella kaikkialle muualleki, esim. eri guiStateille eri draw methodei jne.
	
	polish esim. kattoa mit� partseja piirt�� miss�ki GameState (sit� ennen kommentoidut osat pois jossei tarvi)
	=> Esim. healthBarit ja attackTimerit piirt�� vain Game:ssa
	==> Towerien attackTimerit nollata, kun wave completed
	===> Kattella onko nollattava (ja alustettava) kohta 0 vai attackSpeed eli
		 voiko hy�k�t� heti kun on placed vai odottaako ensimm�isenkin iskun
	
	enemien poistaminen ja lives-- kun endTileen p��tyy
	(tai ruudun ulkopuolella + hasSpawned + varotoimet koska spawnaa ruudun ulkopuolelle)
	
	scale static => refactor kaik luokat sen mukaisesti, also cameraX/Y static Get?
=> mapin tilesize ja getScaledTileSize() staattisiks kans? ku jos scale static nii jeah.

GUI luokka? Palauttaa vaik palautetun buttonin id:n tai jtn tai sit public getPressedButton() tjsp.
=> vois encapsulate helpommin ehk� jos piilotettava GUI
==> drag open/close. Eli alottaa painamisen siit nuolesta nii voi drag open/close ja jos tarpeeks raahaa tiettyyn suuntaan riippuen ofc kumpi state p��ll�. Sit se liikkuu tasasen (?) nopeaa loppu matkan ja SIT vast usable.

tower stats leveys ja korkeus tekstien leveyksien ja korkeuksien mukaan

restrictionii, ett� pakko olla v�hint��n 1 tower, eli ku tower creationissa on deleteem�ss� vikaa j�lel olevaa towerii nii tulee viestii ettei voi delete, oltava v�hint��n 1 tower

enemyn k��ntyminen
attacktimer vain selected towerille?

	tarvii vain kerran painaa samaa tile� ostettavan towerin valittua, jos painaa sit� josta osto aloitettiin
	=> korjata tai ohjeistaa? kun joutuu muuta tile� painamaan sitten kahdesti, jos muualle siis tahtoo towerin laittaa
	
	clampcameraan vai et jos x/y < 1, x/y = 0 ja sama ehk� right ja bottomiin kanssa.
	=> Suaattais auttaa ku nyt vissiin vasemmal saattaa x = 0 pikselit kadota
	
	joku tapa clearata selected tile tai olla n�ytt�m�tt� sit� drawRectii sillo ku vain yhesti painanu tile�?
	
	jtn jolla cancel tower placing (tai tarviiko sittenk��n, ku jos laittaa collision tileen?)
	
	Enemy antaa olla unscaled jos scale vois vaihtua
	
	kattella mit� taree public&private, esim. Enemy.GetDrawX/Y() tarviiko olla public?
	funktioita v�hemm�ks, esim. Enemy.GetScaledOriginX/Y() pit�sk� poistaa ku voi ottaa origin * scale. My�s mapissa paljon funkkareit
	tileMap => map esim. Enemyss�
	Enemy GetDrawX/Y() huonot nimet, oikeasti top-left world x/y
	tower buying pois kun win/loss
	
	finish up Enemy.bounds
	projectile.bounds laittaa
	projectile UNSCALEDIKS hngh
*/
	
	public void Update(long elapsedMilliseconds)
	{
		if (metaGameState == MetaGameState.Menu)
		{
			
		}
		else if (metaGameState == MetaGameState.Create)
		{
			
		}
		else if (metaGameState == MetaGameState.Game)
		{
			if (gameState == GameState.Game)
				UpdateGame(elapsedMilliseconds);
		}
	}
	
	public void Draw(Canvas canvas, long timeElapsedMilliseconds)
	{
		canvas.drawColor(Color.DKGRAY);
		
		if (metaGameState == MetaGameState.Menu)
		{
			
		}
		else if (metaGameState == MetaGameState.Create)
		{
			
		}
		else if (metaGameState == MetaGameState.Game)
		{
			map.Draw(canvas, cameraX, cameraY);
			
			if (selectedTower != INVALID_TOWER)
				DrawSelectedTowerCircles(canvas);
			else if (selectedTileX != INVALID_TILE && selectedTileY != INVALID_TILE)
			{
				if (map.IsCollisionTile(selectedTileX, selectedTileY))
					paint.setColor(0xFFAA2222);
				else
					paint.setColor(0xFF229922);
				
				paint.setStyle(Style.FILL);
				
				canvas.drawRect(
						selectedTileX * Map.GetScaledTileSize() - cameraX,
						selectedTileY * Map.GetScaledTileSize() - cameraY,
						(selectedTileX + 1) * Map.GetScaledTileSize() - 1 - cameraX,
						(selectedTileY + 1) * Map.GetScaledTileSize() - 1 - cameraY, paint);
				
				if (guiState == GUIState.TowerPlacing)
				{
					paint.setStyle(Style.FILL);
					paint.setColor(Color.DKGRAY);
					paint.setAlpha(25);
					
					canvas.drawCircle(
							(selectedTileX + 0.5f) * Map.GetScaledTileSize() - cameraX,
							(selectedTileY + 0.5f) * Map.GetScaledTileSize() - cameraY,
							userTowers[towerToBuy].GetRangeTiles() * Map.GetScaledTileSize(), paint);
				}
			}
			
			if (currentWave < enemyWaveList.size())
				enemyWaveList.get(currentWave).Draw(canvas, cameraX, cameraY);
			
			for (int i = 0; i < towerList.size(); i++)
				towerList.get(i).Draw(canvas, Map.GetScaledTileSize(), cameraX, cameraY, paint);
			
			if (gameState != GameState.Win)
				enemyWaveList.get(currentWave).DrawHealthBars(canvas, paint, Map.GetScaledTileSize(), cameraX, cameraY);
			
			if (guiState == GUIState.TowerBuying)
				DrawTowerBuyingGUI(canvas);
			else if (guiState == GUIState.TowerPlacing)
				DrawTowerPlacing(canvas);
			
			DrawSelectedTowerInfo(canvas);
			
			DrawGameStateText(canvas);
			
			if (gameState == GameState.WaveNotStarted)
				canvas.drawBitmap(buttonStart, gameView.Width - buttonStart.getWidth(), gameView.Height - buttonStart.getHeight(), null);
		}
	}
	
	public void OnPause()
	{
	}
	
	public void UpdateTouchDown(int pointerID, float touchX, float touchY)
	{
		if (pointerInUseID == INVALID_POINTER || pointerID == pointerInUseID)
		{
			pointerX = touchX;
			pointerY = touchY;
		}
		
		if (GetWorldX(touchX) >= 0 && GetWorldY(touchY) >= 0 &&
				GetWorldX(touchX) < map.GetPixelWidth() &&
				GetWorldY(touchY) < map.GetPixelHeight())
		{
			if (pointerInUseID == INVALID_POINTER)
			{
				pointerInUseID = pointerID;
				didClick = true;
			}
		}
		else
		{
			pointerInUseID = INVALID_POINTER;
			didClick = false;
			guiState = GUIState.None;
		}
	}
	
	public void UpdateTouchMove(int pointerID, float touchX, float touchY)
	{
		didClick = false;
		
		if (pointerID == pointerInUseID)
		{
			cameraX -= touchX - pointerX;
			cameraY -= touchY - pointerY;
			
			pointerX = touchX;
			pointerY = touchY;
			
			ClampCameraPosition();
		}
	}
	
	public void UpdateTouchUp(int pointerID, float touchX, float touchY)
	{
		if (pointerID == pointerInUseID)
		{
			pointerInUseID = INVALID_POINTER;
			
			if (didClick)
			{
				selectedTower = -1;
				
				if (gameState == GameState.WaveNotStarted)
				{
					if (touchX >= gameView.Width - buttonStart.getWidth() &&
						touchY >= gameView.Height - buttonStart.getHeight())
					{
						gameState = GameState.Game;
						didClick = false;
						ResetSelectedTiles();
						
						return;
					}
				}
				
				if (guiState != GUIState.TowerBuying)// && (gameState == GameState.Game || gameState == GameState.Win))
				{
					selectedTileX = (int)Math.floor(GetWorldX(touchX) / Map.GetScaledTileSize());
					selectedTileY = (int)Math.floor(GetWorldY(touchY) / Map.GetScaledTileSize());
				}
				
				if (guiState == GUIState.None)// && (gameState == GameState.Game || gameState == GameState.Win))
				{
					if (map.IsTowerOccupied(selectedTileX, selectedTileY))
					{
						for (int i = 0; i < towerList.size(); i++)
						{
							if (towerList.get(i).TileX == selectedTileX && towerList.get(i).TileY == selectedTileY)
							{
								selectedTower = i;
								break;
							}
						}
						
						didClick = false;
						ResetSelectedTiles();
						return;
					}
				}
				
				if (gameState != GameState.Win)
				{
					if (guiState == GUIState.TowerBuying)
					{
						int iconScale = 2;
						
						float halfIconWidth = 0, halfIconHeight = 0;
						for (int i = 0; i < userTowers.length; i++)
						{
							if (userTowers[i] != null)
							{
								halfIconWidth = userTowers[i].GetBitmap().getWidth() * 0.5f * iconScale;
								halfIconHeight = userTowers[i].GetBitmap().getHeight() * 0.5f * iconScale;
								break;
							}
						}
						
						float halfWindowWidth = halfIconWidth * 4 + halfIconWidth * 5 / 2;
						float halfWindowHeight = halfIconHeight * 2 + (halfIconHeight * 4 + buttonSelect.getHeight()) / 2;
						
						float firstX = (gameView.HalfWidth - halfWindowWidth) + halfIconWidth;
						float firstY = (gameView.HalfHeight - halfWindowHeight) + halfIconHeight;
						
						Boolean didPressButton = false;
						if (touchX >= gameView.HalfWidth - buttonSelect.getWidth() - halfIconWidth / 2 + 1 &&
							touchY >= firstY + halfIconHeight * 6 &&
							touchX < gameView.HalfWidth - halfIconWidth / 2 + 1 &&
							touchY < firstY + halfIconHeight * 6 + buttonSelect.getHeight())
						{
							if (userTowers[towerToBuy] != null)
							{
								guiState = GUIState.TowerPlacing;
							}
							//else
							//{
							//	guiState = GUIState.None;
							//	towerToBuy = 0;
							//}
							
							didPressButton = true;
						}
						else if (touchX >= gameView.HalfWidth + halfIconWidth / 2 &&
							touchY >= firstY + halfIconHeight * 6 &&
							touchX < gameView.HalfWidth + halfIconWidth / 2 + buttonCancel.getWidth() &&
							touchY < firstY + halfIconHeight * 6 + buttonCancel.getHeight())
						{
							guiState = GUIState.None;
							didPressButton = true;
							towerToBuy = 0;
							ResetSelectedTiles();
						}
						
						if (!didPressButton)
						{
							for (int i = 0; i < 4; i++)
							{
								if (touchX >= firstX + i * (halfIconWidth * 3) &&
									touchY >= firstY &&
									touchX < firstX + halfIconWidth * 2 + i * (halfIconWidth * 3) &&
									touchY < firstY + halfIconHeight * 2)
									towerToBuy = i;
								
								if (touchX >= firstX + i * (halfIconWidth * 3) &&
										touchY >= firstY + halfIconHeight * 3 &&
										touchX < firstX + halfIconWidth * 2 + i * (halfIconWidth * 3) &&
										touchY < firstY + halfIconHeight * 5)
									towerToBuy = i + 4;
							}
						}
					}
					else
					{
						if (selectedTileX == prevTileX && selectedTileY == prevTileY)
						{
							if (guiState == GUIState.None)
							{
								guiState = GUIState.TowerBuying;
								selectedTower = INVALID_TOWER;
							}
							else if (guiState == GUIState.TowerPlacing)
							{
								if (!map.IsCollisionTile(selectedTileX, selectedTileY))
									PlaceTower();
								else
									guiState = GUIState.None;
								
								ResetSelectedTiles();
							}
						}
					}
					
					didClick = false;
					prevTileX = selectedTileX;
					prevTileY = selectedTileY;
				}
			}
		}
	}
	
	public float GetWorldX(float screenX) { return screenX + cameraX; }
	public float GetWorldY(float screenY) { return screenY + cameraY; }
	
	public void ClampCameraPosition()
	{
		if (gameView.Width > map.GetPixelWidth())
		{
			cameraX = Math.round(map.GetPixelWidth() / 2.0f - gameView.HalfWidth);
		}
		else
		{
			if (cameraX < 0)
				cameraX = 0;
			else if (cameraX + gameView.Width > map.GetPixelWidth())
				cameraX = map.GetPixelWidth() - gameView.Width;
		}
		
		if (gameView.Height > map.GetPixelHeight())
		{
			cameraY = Math.round(map.GetPixelHeight() / 2.0f - gameView.HalfHeight);
		}
		else
		{
			if (cameraY < 0)
				cameraY = 0;
			else if (cameraY + gameView.Height > map.GetPixelHeight())
				cameraY = map.GetPixelHeight() - gameView.Height;
		}
	}
	
	private void UpdateGame(long elapsedMilliseconds)
	{
		enemyWaveList.get(currentWave).Update(elapsedMilliseconds, map, cameraX, cameraY);
		
		lives -= enemyWaveList.get(currentWave).GetEnemiesGotAwayCount();
		
		if (lives < 1)
		{
			gameState = GameState.Loss;
			lives = 0;
			return;
		}
		
		for (int i = 0; i < towerList.size(); i++)
			towerList.get(i).Update(elapsedMilliseconds, enemyWaveList.get(currentWave).EnemyList, Map.GetScaledTileSize());
		
		CheckProjectileEnemyCollisions();
		
		if (enemyWaveList.get(currentWave).HasFinished)
		{
			currentWave++;
			guiState = GUIState.None;
			selectedTower = INVALID_TOWER;
			
			for (int i = 0; i < towerList.size(); i++)
				towerList.get(i).UpdateEndOfRound();
			
			if (currentWave >= enemyWaveList.size())
				gameState = GameState.Win;
			else
				gameState = GameState.WaveNotStarted;
		}
	}
	
	private void DrawTowerBuyingGUI(Canvas canvas)
	{
		float iconScale = 2;
		
		float halfIconWidth = 0, halfIconHeight = 0;
		for (int i = 0; i < userTowers.length; i++)
		{
			if (userTowers[i] != null)
			{
				halfIconWidth = userTowers[i].GetBitmap().getWidth() * 0.5f * iconScale;
				halfIconHeight = userTowers[i].GetBitmap().getHeight() * 0.5f * iconScale;
				break;
			}
		}
		
		float halfWindowWidth = halfIconWidth * 4 + halfIconWidth * 5 * 0.5f;
		float halfWindowHeight = halfIconHeight * 2 + (halfIconHeight * 4 + buttonSelect.getHeight()) * 0.5f;

		paint.setStyle(Style.FILL);
		paint.setColor(Color.LTGRAY);
		paint.setAlpha(100);
		canvas.drawRect(
				gameView.HalfWidth - halfWindowWidth, gameView.HalfHeight - halfWindowHeight,
				gameView.HalfWidth + halfWindowWidth, gameView.HalfHeight + halfWindowHeight, paint);

		paint.setStyle(Style.STROKE);
		paint.setColor(Color.DKGRAY);
		paint.setAlpha(150);
		canvas.drawRect(
				gameView.HalfWidth - halfWindowWidth, gameView.HalfHeight - halfWindowHeight,
				gameView.HalfWidth + halfWindowWidth, gameView.HalfHeight + halfWindowHeight, paint);
		
		float firstX = (gameView.HalfWidth - halfWindowWidth) + halfIconWidth;
		float firstY = (gameView.HalfHeight - halfWindowHeight) + halfIconHeight;

		paint.setStyle(Style.FILL);
		paint.setColor(0xFF888888);
		paint.setAlpha(100);
		canvas.drawRect(
				firstX + (towerToBuy % 4) * (halfIconWidth * 3),
				firstY + ((towerToBuy - towerToBuy % 4) / 4) * (halfIconHeight * 3),
				firstX + halfIconWidth * 2 + (towerToBuy % 4) * (halfIconWidth * 2 + halfIconWidth),
				firstY + halfIconHeight * 2 + ((towerToBuy - towerToBuy % 4) / 4) * (halfIconHeight * 3), paint);

		for (int i = 0; i < 4; i++)
		{
			paint.setStyle(Style.STROKE);
			paint.setColor(Color.DKGRAY);
			paint.setAlpha(150);

			canvas.drawRect(
					firstX + i * (halfIconWidth * 3),
					firstY,
					firstX + halfIconWidth * 2 + i * (halfIconWidth * 2 + halfIconWidth),
					firstY + halfIconHeight * 2, paint);
			canvas.drawRect(
					firstX + i * (halfIconWidth * 3),
					firstY + halfIconHeight * 3,
					firstX + halfIconWidth * 2 + i * (halfIconWidth * 2 + halfIconWidth),
					firstY + halfIconHeight * 5, paint);
			
			paint.setAlpha(255);
			if (userTowers[i] != null)
				canvas.drawBitmap(userTowers[i].GetBitmap(), null,
						new RectF(
								firstX + i * (halfIconWidth * 3),
								firstY,
								firstX + halfIconWidth * 2 + i * (halfIconWidth * 2 + halfIconWidth),
								firstY + halfIconHeight * 2), paint);

			if (userTowers[i + 4] != null)
				canvas.drawBitmap(userTowers[i + 4].GetBitmap(), null,
						new RectF(
								firstX + i * (halfIconWidth * 3),
								firstY + halfIconHeight * 3,
								firstX + halfIconWidth * 2 + i * (halfIconWidth * 2 + halfIconWidth),
								firstY + halfIconHeight * 2 + halfIconHeight * 3), paint);
		}

		canvas.drawBitmap(buttonSelect, gameView.HalfWidth - buttonSelect.getWidth() - halfIconWidth / 2 + 1, firstY + halfIconHeight * 6, null);
		canvas.drawBitmap(buttonCancel, gameView.HalfWidth + halfIconWidth / 2, firstY + halfIconHeight * 6, null);
	}
	
	private void DrawTowerPlacing(Canvas canvas)
	{
		RectF rectangleBounds = new RectF(
				map.GetTilePixelPositionX(selectedTileX) - cameraX,
				map.GetTilePixelPositionY(selectedTileY) - cameraY,
				map.GetTilePixelPositionX(selectedTileX + 1) - cameraX - 1,
				map.GetTilePixelPositionY(selectedTileY + 1) - cameraY - 1);

		if (map.IsCollisionTile(selectedTileX, selectedTileY))
			paint.setColor(0xFFAA2222);
		else
			paint.setColor(0xFF229922);

		paint.setStyle(Style.FILL);
		//canvas.drawRect(rectangleBounds, paint); //Piirret��n Drawissa, kun selectedTileX/Y != INVALID_TILE => se ALLE ja bitmap P��LLE vihollisten

		paint.setColor(Color.WHITE);
		canvas.drawBitmap(userTowers[towerToBuy].GetBitmap(), null, rectangleBounds, paint);
	}
	
	private void DrawSelectedTowerCircles(Canvas canvas)
	{
		if (selectedTower == INVALID_TOWER)
			return;
		
		paint.setStyle(Style.FILL);
		paint.setColor(Color.DKGRAY);
		paint.setAlpha(25);
		
		canvas.drawCircle(
				(towerList.get(selectedTower).TileX + 0.5f) * Map.GetScaledTileSize() - cameraX,
				(towerList.get(selectedTower).TileY + 0.5f) * Map.GetScaledTileSize() - cameraY,
				towerList.get(selectedTower).GetRangeTiles() * Map.GetScaledTileSize(), paint);
		
		paint.setColor(0xFF559900);
		
		canvas.drawCircle(
				(towerList.get(selectedTower).TileX + 0.5f) * Map.GetScaledTileSize() - cameraX,
				(towerList.get(selectedTower).TileY + 0.5f) * Map.GetScaledTileSize() - cameraY,
				Map.GetScaledTileSize() * 0.5f, paint);
		
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.DKGRAY);
		
		canvas.drawCircle(
				(towerList.get(selectedTower).TileX + 0.5f) * Map.GetScaledTileSize() - cameraX,
				(towerList.get(selectedTower).TileY + 0.5f) * Map.GetScaledTileSize() - cameraY,
				Map.GetScaledTileSize() * 0.5f, paint);
	}
	
	private void DrawSelectedTowerInfo(Canvas canvas)
	{
		if (selectedTower == INVALID_TOWER)
			return;
		
		paint.setColor(Color.LTGRAY);
		paint.setAlpha(150);
		paint.setStyle(Style.FILL);
		
		canvas.drawRect(5, gameView.Height - 90.0f, 155.0f, gameView.Height - 5, paint);
		
		paint.setColor(Color.DKGRAY);
		paint.setAlpha(150);
		paint.setStyle(Style.STROKE);
		
		canvas.drawRect(5, gameView.Height - 90.0f, 155.0f, gameView.Height - 5, paint);
		
		paint.setColor(Color.WHITE);
		paint.setTextSize(20);
		paint.setTextAlign(Align.LEFT);
		
		canvas.drawText("DAMAGE:", 10, gameView.Height - 70, paint);
		canvas.drawText("RANGE:", 10, gameView.Height - 50, paint);
		canvas.drawText("SPEED:", 10, gameView.Height - 30, paint);
		canvas.drawText("KILL COUNT:", 10, gameView.Height - 10, paint);
		
		paint.setTextAlign(Align.RIGHT);
		
		float x = paint.measureText("KILL COUNT: 100");
		canvas.drawText("" + towerList.get(selectedTower).GetDamage(), 10 + x, gameView.Height - 70, paint);
		canvas.drawText("" + towerList.get(selectedTower).GetRangeTiles(), 10 + x, gameView.Height - 50, paint);
		canvas.drawText("" + towerList.get(selectedTower).GetSpeed(), 10 + x, gameView.Height - 30, paint);
		canvas.drawText("" + towerList.get(selectedTower).KillCount, 10 + x, gameView.Height - 10, paint);
	}
	
	private void DrawGameStateText(Canvas canvas)
	{
		if (gameState == GameState.Game)
		{
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.LEFT);
			paint.setTextSize(20);
			canvas.drawText("Lives: " + lives, 5, 20, paint);
		}
		else if (gameState == GameState.WaveNotStarted)
		{
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(20);
			canvas.drawText("Press the Start button to begin wave " + (currentWave + 1) + ".", gameView.HalfWidth, gameView.HalfHeight + 15, paint);
		}
		else if (gameState == GameState.Win)
		{
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(20);
			if (enemyWaveList.size() == 1)
				canvas.drawText("You win after 1 wave!", gameView.HalfWidth, gameView.HalfHeight + 15, paint);
			else
				canvas.drawText("You win after " + enemyWaveList.size() + " waves!", gameView.HalfWidth, gameView.HalfHeight + 15, paint);
		}
		else if (gameState == GameState.Loss)
		{
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(20);
			if (currentWave == 0)
				canvas.drawText("You lose after...", gameView.HalfWidth, gameView.HalfHeight + 15, paint);
			else if (currentWave == 1)
				canvas.drawText("You lose after 1 wave...", gameView.HalfWidth, gameView.HalfHeight + 15, paint);
			else
				canvas.drawText("You lose after " + currentWave + " waves...", gameView.HalfWidth, gameView.HalfHeight + 15, paint);
		}
	}
	
	private void PlaceTower()
	{
		towerList.add(new Tower(userTowers[towerToBuy], selectedTileX, selectedTileY));
		map.PlacedTower(selectedTileX, selectedTileY);
		
		towerToBuy = 0;
		selectedTower = towerList.size() - 1;
		guiState = GUIState.None;
	}
	
	private void CheckProjectileEnemyCollisions()
	{
		RectF[] projectileBounds;
		for (int t = 0; t < towerList.size(); t++)
		{
			projectileBounds = towerList.get(t).GetProjectileBounds();
			
			if (projectileBounds == null)
				continue;
			
			for (int p = 0; p < projectileBounds.length; p++)
			{
				for (int e = 0; e < enemyWaveList.get(currentWave).EnemyList.size(); e++)
				{
					if (projectileBounds[p].intersect(enemyWaveList.get(currentWave).EnemyList.get(e).GetBounds()))
					{
						enemyWaveList.get(currentWave).EnemyList.get(e).Damage(towerList.get(t).GetProjectile(p).Damage);
						
						if (!enemyWaveList.get(currentWave).EnemyList.get(e).IsAlive())
							towerList.get(t).KillCount++;
						
						towerList.get(t).GetProjectile(p).DidHit = true;
						break;
					}
				}
			}
		}
	}
	
	private void ResetSelectedTiles()
	{
		selectedTileX = INVALID_TILE;
		selectedTileY = INVALID_TILE;
		prevTileX = INVALID_TILE;
		prevTileY = INVALID_TILE;
	}
}
