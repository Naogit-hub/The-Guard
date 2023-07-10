import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Camera;

import javafx.animation.AnimationTimer;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class OriginalTileGame extends Application {
    private int initialSceneWidth = 600;
    private int initialSceneHeight = 500;
    private int gameStatus = -2;

    private MapAndChars mapAndChars;
    private Camera camera = new PerspectiveCamera();
    private Stage st;

    Group root = new Group(); // ゲーム画面
    Group titleRoot = new Group(); // ゲームタイトル
    Group descriptionRoot = new Group(); // ゲームの説明

    Scene gameScene = new Scene(root, initialSceneWidth, initialSceneHeight, Color.rgb(0, 0, 0));
    Scene titleScene = new Scene(titleRoot, initialSceneWidth, initialSceneHeight, Color.rgb(0, 0, 0));
    Scene descriptionScene = new Scene(descriptionRoot, initialSceneWidth, initialSceneHeight, Color.rgb(0, 0, 0));

    @Override
    public void start(Stage st) throws Exception {
	this.st = st;

	titleScene.setOnKeyPressed(this::keyPressed);
	descriptionScene.setOnKeyPressed(this::keyPressed);
	gameScene.setOnKeyPressed(this::keyPressed);
	drawTitle();
    }

    public static void main(String[] a) {
	launch(a);
    }

    public void keyPressed(KeyEvent e) {
	KeyCode key = e.getCode();
	int dir = -1;

	if (gameStatus == 0) {
	    String st = mapAndChars.p.getStatus();
	    switch (key) {
	    case LEFT:
		if (st.equals("NEUTRAL"))
		    dir = 2;
		break;// left
	    case RIGHT:
		if (st.equals("NEUTRAL"))
		    dir = 0;
		break;// right
	    case UP:
		if (st.equals("NEUTRAL"))
		    dir = 1;
		break;// up
	    case DOWN:
		if (st.equals("NEUTRAL"))
		    dir = 3;
		break;// down
	    case C:
		if (st.equals("NEUTRAL")) {
		    mapAndChars.tmpMap = mapAndChars.p.posY;
		    mapAndChars.p.setStatus("JUMP");
		} else if (st.equals("JUMP")) {
		    mapAndChars.p.setStatus("DOUBLEJUMP");
		}
		break;// jump
	    case X: // 攻撃
		if (st.equals("NEUTRAL")) {
		    mapAndChars.p.setStatus("ATTACK");
		}
		break;
		/* 確認用↓ */
	    case S:
		System.out.println(mapAndChars.p.getStatus());
		break;
	    case M: // mapを表示
		mapAndChars.openMap();
		break;
	    default:
		return;
	    }

	    if (dir >= 0) {
		mapAndChars.p.setDirection(dir);
		mapAndChars.movePlayer();
	    }
	} else if (gameStatus < 0) { // ゲームスタート前（タイトル画面）
	    switch (key) {
	    case ENTER:
		if (gameStatus == -2) { // -2 ... title
		    drawDescription();
		} else { // -1 ... description
		    gameStart();
		}
		break;

	    default:
		break;
	    }
	    return;
	}
    }

    public void drawTitle() {
	st.setTitle("The Guard");
	st.setScene(titleScene);
	st.show();

	Text title = new Text(190, 150, "The Guard");
	Text rule = new Text(180, 200, "~~姫を護衛しろ！~~");
	Text start = new Text(180, 330, "次へ:   ENTER");
	title.setFont(new Font("", 50));
	title.setFill(Color.RED);
	rule.setFont(new Font("", 30));
	rule.setFill(Color.RED);
	start.setFont(new Font("", 40));
	start.setFill(Color.WHITE);
	titleRoot.getChildren().addAll(title, start, rule);
    }

    public void gameStart() {
	gameStatus = 0; // ゲームスタート
	mapAndChars = new MapAndChars(st, root, camera, gameStatus);

	gameScene.setCamera(camera);
	st.setScene(gameScene);
	st.show();

	AnimationTimer timer = new AnimationTimer() {
		long startTime = 0;

		@Override
		public void handle(long t) {
		    if (startTime == 0) {
			startTime = t;
		    }
		    mapAndChars.drawScreen((int) ((t - startTime) / 1000000000));
		}
	    };

	timer.start();
    }

    public void drawDescription() {
	gameStatus = -1;
	st.setTitle("The Guard");
	st.setScene(descriptionScene);
	st.show();

	Text crossKey = new Text(120, 150, "十字キー: 移動");
	Text atkKey = new Text(120, 200, "X: 攻撃");
	Text jumpKey = new Text(120, 250, "C: ジャンプ、2段ジャンプ");
	Text start = new Text(120, 370, "ゲームスタート: ENTER");

	crossKey.setFont(new Font("", 40));
	crossKey.setFill(Color.WHITE);
	atkKey.setFont(new Font("", 40));
	atkKey.setFill(Color.WHITE);
	jumpKey.setFont(new Font("", 40));
	jumpKey.setFill(Color.WHITE);
	start.setFont(new Font("", 40));
	start.setFill(Color.WHITE);
	descriptionRoot.getChildren().addAll(crossKey, atkKey, jumpKey, start);
    }
}

class MapAndChars {
    Group root;
    Stage st;
    Camera camera;

    private char[][] map; // キャラクター(Enemy, Player, NPC, Item等々)の存在を管理(アニメーションが適用された世界)
    private char[][] field; // フィールド(ブロック・はしご等々)ほとんど(後にブロックを置く・破壊などを追加)普遍的なフィールドを構成するオブジェクトの管理(最初の世界)
    private int MX = 64, MY = 7;
    private String[] initialMap = { "                                                                ",
				    "                                                    E           ",
				    "             E           H   E                   LBBBBBBB   E   ",
				    " P        LBBBBBB       BBBBBBB              E   L  E       B   ",
				    " B        L     EBB LBB       E    E      LBBBBBBBBBB           ",
				    " N       ELE        L E E   ELB    LB     L                    G",
				    "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" };

		/* 縮小版map  横スクロールが不可能の場合
		 * 
		 * private int MX = 32, MY = 7;
		 * private String[] initialMap = 
		 * 		{ "                                ",
				    "                                ",
				    "             E               E  ",
				    " P     LBBBBBBB   H   E BBBBBB  ",
				    " B     L         BB LBB         ",
				    " N     L     E      L       E  G",
				    "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" };
		 * 
		 * 
		 * 
		 */

    int gameStatus; // ゲームの状態を管理
    int initialSceneWidth = 600; // ゲームの横幅

    // アニメーション処理ごとのflame数
    int atk_flame30 = 30; // atttack
    int col_flame30 = 30; // collision
    int jump_flame7 = 7; // jump
    int dead_pflame60 = 60; // dead
    // int respawn_eflame300 = 300;

    Text timeText;

    /* NPC */
    NPC npc;
    Image girlImage01 = new Image("./girl_right1.png");
    Image girlImage02 = new Image("./girl_right2.png");
    Image girlImage03 = new Image("./girl_right3.png");
    Image npc_cI01 = new Image("./girl_climb.png");

    /* Player */
    Player p;
    int playerImageTime = 0;
    int ableMove = 0;
    int tmpMap; // cを押した位置のp.posYを格納

    /* HP */
    final int HP = 4; // 初期HP
    List<Life> totalLife = new ArrayList<Life>();

    /* 右 */
    Image p_rI01 = new Image("./boy_right1.png");
    Image p_rI02 = new Image("./boy_right2.png");
    Image p_rI03 = new Image("./boy_right3.png");

    /* 左 */
    Image p_lI01 = new Image("./boy_left1.png");
    Image p_lI02 = new Image("./boy_left2.png");
    Image p_lI03 = new Image("./boy_left3.png");

    /* 登り降り */
    Image p_cI01 = new Image("./boy_climb1.png");
    Image p_cI02 = new Image("./boy_climb2.png");

    /* 攻撃 */
    Image p_rStick01 = new Image("./boy_r_stick1.png");
    Image p_rStick02 = new Image("./boy_r_stick2.png");
    Image p_rStick03 = new Image("./boy_r_stick3.png");
    Image p_rStick04 = new Image("./boy_r_stick4.png");
    Image p_lStick01 = new Image("./boy_l_stick1.png");
    Image p_lStick02 = new Image("./boy_l_stick2.png");
    Image p_lStick03 = new Image("./boy_l_stick3.png");
    Image p_lStick04 = new Image("./boy_l_stick4.png");

    // Block
    Image blockImage;
    ImageView blockView;

    // Laddar
    Image laddarImage;
    ImageView laddarView;

    /* Enemy */
    Enemy e;
    List<Enemy> enemyList = new ArrayList<Enemy>();
    Image e_lI01 = new Image("./E1_left.png");
    Image e_lI02 = new Image("./E2_left.png");
    Image e_rI01 = new Image("./E1_right.png");

    /* Item */
    Item heart;
    Image heartPlusImage = new Image("./life_plus.png");

    /* Goal */
    Goal goal;
    Image goalImage01 = new Image("./goal_torch12.png");
    Image goalImage02 = new Image("./goal_torch22.png");

    MapAndChars(Stage st, Group root, Camera camera, int gameStatus) {
	// マップの周囲を見えない壁で囲む
	this.st = st;
	this.camera = camera;
	this.gameStatus = gameStatus;
	map = new char[MY + 2][MX + 2];
	field = new char[MY + 2][MX + 2];
	for (int x = 0; x <= MX + 1; x++) {
	    map[0][x] = 'B';
	    map[MY + 1][x] = 'B';
	    field[0][x] = 'B';
	    field[MY + 1][x] = 'B';
	}
	for (int y = 0; y <= MY + 1; y++) {
	    map[y][0] = 'B';
	    map[y][MX + 1] = 'B';
	    field[y][0] = 'B';
	    field[y][MX + 1] = 'B';
	}
	// マップデータの読み込み
	for (int y = 1; y <= MY; y++) {
	    for (int x = 1; x <= MX; x++) {
		char block = initialMap[y - 1].charAt(x - 1);
		map[y][x] = block;
		if (block != 'P' && block != 'N' && block != 'E' && block != 'H') {
		    field[y][x] = block; // field背景のみ
		} else {
		    field[y][x] = ' ';
		}
	    }
	}

	drawInitialMapAndChars(root);
    }

    public void drawScreen(int t) {
	switch (gameStatus) {
	case 0: // keep gaming
	    drawBackGround(t); // 背景描画（時間・体力）
	    moveEnemy(); // Enemy の運動
	    moveNPC(t); // NPC の運動
	    collisionPlayer(); // Player の衝突
	    collisionNPC(); // NPC の衝突
	    playerStatusAction(); // player のstatus ごとのアニメーション処理
	    break;

	case 1: // game clear
	case 2: // game over
	    drawResult(st);
	    break;

	default:
	    break;
	}
	return;
    }

    public void drawInitialMapAndChars(Group root) {
	timeText = new Text(40, 70, " ");
	timeText.setFont(new Font("TimeRoman", 30));
	root.getChildren().add(timeText);

	for (int i = 0; i < HP; i++) {
	    Life l = new Life();
	    totalLife.add(l);
	    root.getChildren().add(l.view);
	}

	for (int y = 1; y <= MY; y++) {
	    for (int x = 1; x <= MX; x++) {
		switch (map[y][x]) {
		    // 地面ブロックの描画
		case 'B':
		    blockImage = new Image("./block.png");
		    blockView = new ImageView(blockImage);
		    blockView.setX(40 * x);
		    blockView.setY(40 * y);
		    root.getChildren().add(blockView);
		    break;
		    // はしごの描画
		case 'L':
		    laddarImage = new Image("./laddarB.png");
		    laddarView = new ImageView(laddarImage);
		    laddarView.setX(40 * x);
		    laddarView.setY(40 * y);
		    root.getChildren().add(laddarView);
		    break;
		    // エネミー描画
		case 'E':
		    e = new Enemy(x, y, e_lI02, map, field, 'E');
		    enemyList.add(e);
		    root.getChildren().add(e.getCharView());
		    break;
		    // プレイヤー描画
		case 'P':
		    p = new Player(x, y, p_rStick01, map, field, 'P');
		    root.getChildren().add(p.getCharView());
		    p.setHP(HP);
		    break;
		    // NPC描画
		case 'N':
		    npc = new NPC(x, y, girlImage01, map, field, 'N');
		    root.getChildren().add(npc.getCharView());
		    break;
		    // アイテム(プラスワン)の描画
		case 'H':
		    heart = new Item(x, y, heartPlusImage, map, field, 'H');
		    root.getChildren().add(heart.getCharView());
		    break;
		    // ゴールの描画
		case 'G':
		    goal = new Goal(x, y, goalImage01, map, field, 'G');
		    root.getChildren().add(goal.getCharView());
		    break;
		default:
		    break;
		}
	    }
	}
    }

    public void moveEnemy() {
	for (int i = 0; i < enemyList.size(); i++) {
	    Enemy tmp = enemyList.get(i);
	    if (tmp == null) {
	    } else {
		switch (tmp.getStatus()) {
		case "NEUTRAL":
		    if (tmp.noticeObject(7, npc) || tmp.noticeObject(4, p)) {
			// EnemyがPlayerを発見する処理(3マス)
			tmp.setDirection(2);
			tmp.setStatus("HOSTILITY");
		    }
		    break;

		case "HOSTILITY":
		    if (tmp.getDirection() == 0) {
			tmp.view.setImage(e_rI01);
			if (field[tmp.posY][tmp.posX + 1] == 'B') {
			    tmp.setDirection(-1);
			}
		    } else if (tmp.getDirection() == 2) {
			tmp.view.setImage(e_lI01);
			if (field[tmp.posY][tmp.posX - 1] == 'B') {
			    tmp.setDirection(-1);
			}
		    }
		    if (field[tmp.posY + 1][tmp.posX] == ' ') {
			tmp.fall();
		    } else {
			tmp.move(1);
		    }
		    break;

		case "DEAD":
		    tmp.view.setImage(new Image("./grave.png"));
		    enemyList.set(i, null);
		    break;
		}
	    }
	}
    }

    public void movePlayer() {
	if (ableMove % 2 == 0) { // ableMove２フレームに１回
	    switch (p.getDirection()) {
	    case 0:
		if (playerImageTime % 4 == 0) {
		    p.view.setImage(p_rStick01);
		    playerImageTime++;
		} else if (playerImageTime % 4 == 1) {
		    p.view.setImage(p_rStick02);
		    playerImageTime++;
		} else if (playerImageTime % 4 == 2) {
		    p.view.setImage(p_rStick03);
		    playerImageTime++;
		} else if (playerImageTime % 4 == 3) {
		    p.view.setImage(p_rStick02);
		    playerImageTime = 0;
		    ableMove = 0;
		}
		break;
	    case 1:
		if (field[p.posY][p.posX] == 'L') {
		    if (playerImageTime % 2 == 0) {
			p.view.setImage(p_cI01);
			playerImageTime++;
		    } else if (playerImageTime % 2 == 1) {
			p.view.setImage(p_cI02);
			playerImageTime = 0;
			ableMove = 0;
		    }
		}
		break;
	    case 2:
		if (playerImageTime % 4 == 0) {
		    p.view.setImage(p_lStick01);
		    playerImageTime++;
		} else if (playerImageTime % 4 == 1) {
		    p.view.setImage(p_lStick02);
		    playerImageTime++;
		} else if (playerImageTime % 4 == 2) {
		    p.view.setImage(p_lStick03);
		    playerImageTime++;
		} else if (playerImageTime % 4 == 3) {
		    p.view.setImage(p_lStick02);
		    playerImageTime = 0;
		    ableMove = 0;
		}
		break;
	    case 3:
		if (field[p.posY + 1][p.posX] == 'L') {
		    if (playerImageTime % 2 == 0) {
			p.view.setImage(p_cI01);
			playerImageTime++;
		    } else if (playerImageTime % 2 == 1) {
			p.view.setImage(p_cI02);
			playerImageTime = 0;
			ableMove = 0;
		    }
		}
		break;
	    }
	    p.move(6);
	    p.view.toFront();
	}
	ableMove++;
    }

    public void moveNPC(int t) {
	switch (npc.getStatus()) {
	case "NEUTRAL":
	    if (field[npc.posY][npc.posX] == 'L') {
		npc.setStatus("CLIMB");
		npc.setDirection(1);
		return;
	    } else if (field[npc.posY + 1][npc.posX] == ' ') {
		npc.setStatus("FALL");
		return;
	    }

	    if (t % 3 == 0) {
		npc.view.setImage(girlImage01);
	    } else if (t % 3 == 1) {
		npc.view.setImage(girlImage02);
	    } else if (t % 3 == 2) {
		npc.view.setImage(girlImage03);
	    }
	    npc.move(0.3);
	    camera.setTranslateX(npc.getNowX() + 20 - initialSceneWidth / 2);
	    break;

	case "CLIMB":
	    npc.view.setImage(npc_cI01);
	    if (field[npc.posY][npc.posX] == ' '
		&& field[npc.posY + 1][npc.posX + 1] == 'B') {
		npc.setStatus("NEUTRAL");
		npc.setDirection(0);
		return;
	    }
	    npc.move(0.3);
	    break;

	case "FALL":
	    if (field[npc.posY + 1][npc.posX] == 'B') {
		npc.setStatus("NEUTRAL");
		npc.setDirection(0);
	    } else {
		npc.fall();
	    }
	    break;
	}
	npc.view.toFront();
    }

    public void playerStatusAction() {
	switch (p.getStatus()) {
	case "NEUTRAL":
	    if (field[p.posY + 1][p.posX] == ' ') {
		p.setStatus("FALL");
	    }
	    break;

	case "JUMP":
	    if ((p.posY + 1) == tmpMap) { // C入力地点より1マス上に到達。
		p.setStatus("FALL");
		return;
	    }
	    if (field[tmpMap - 1][p.posX] == 'B') { // jump失敗
		p.setStatus("NEUTRAL");
	    } else {
		p.jump();
	    }
	    break;

	case "DOUBLEJUMP":
	    if ((p.posY + 2) == tmpMap || field[p.posY - 1][p.posX] == 'B') { // C入力地点より2マス上に到達。
		p.setStatus("DOUBLEFALL");
	    } else {
		if (jump_flame7 > 0) {
		    switch (p.getDirection()) {
		    case 0:
			p.view.setImage(new Image("./boy_jump1.png"));
			break;

		    case 2:
			p.view.setImage(new Image("./boy_jump21.png"));
			break;
		    }
		    jump_flame7--;
		} else {
		    switch (p.getDirection()) {
		    case 0:
			p.view.setImage(p_rStick03);
			break;

		    case 2:
			p.view.setImage(p_lStick01);
			break;
		    }
		    jump_flame7--;
		}

		p.jump();
		p.move(2);
	    }
	    break;

	case "FALL":
	    if (field[p.posY + 1][p.posX] != 'B') { // 下がブロックでない場合落下
		p.fall();
	    } else { // ブロックである場合、接地(NEUTRAL)状態に
		p.setStatus("NEUTRAL");
	    }
	    break;

	case "DOUBLEFALL":
	    if (field[p.posY + 1][p.posX] != 'B') { // 下がブロックでない場合落下
		p.fall();
		p.move(2);
	    } else { // ブロックである場合、接地(NEUTRAL)状態に
		jump_flame7 = 7;
		p.setStatus("NEUTRAL");
	    }
	    break;

	case "ATTACK":
	    switch (p.getDirection()) {
	    case 0:
		if (atk_flame30 > 0) {
		    p.view.setImage(p_rStick04);
		    p.attack(searchEnemy(p.posX + 1, p.posY));
		    atk_flame30--;
		} else {
		    p.setStatus("NEUTRAL");
		    p.view.setImage(p_rStick01);
		    atk_flame30 = 30;
		}
		break;

	    case 2:
		if (atk_flame30 > 0) {
		    p.view.setImage(p_lStick04);
		    p.attack(searchEnemy(p.posX - 1, p.posY));
		    atk_flame30--;
		} else {
		    p.setStatus("NEUTRAL");
		    p.view.setImage(p_lStick01);
		    atk_flame30 = 30;
		}
		break;
	    }
	    break;

	case "FLASH":
	    if (col_flame30 % 2 == 0) {
		p.view.setImage(p_rI01);
	    } else {
		p.view.setImage(new Image("./invisible.png"));
	    }
	    col_flame30--;
	    if (col_flame30 == 0) {
		p.setStatus("NEUTRAL");
		col_flame30 = 30;
	    }
	    break;

	case "DEAD":
	    if (60 >= dead_pflame60 && dead_pflame60 > 30) {
		switch (dead_pflame60 % 4) {
		case 0:
		case 1:
		    p.view.setImage(new Image("./invisible.png"));
		    dead_pflame60--;
		    break;
		case 2:
		case 3:
		    p.view.setImage(p_rI01);
		    dead_pflame60--;
		    break;
		}
	    } else if (30 >= dead_pflame60 && dead_pflame60 > 0) {
		switch (dead_pflame60 % 2) {
		case 0:
		    p.view.setImage(new Image("./invisible.png"));
		    dead_pflame60--;
		    break;
		case 1:
		    p.view.setImage(p_rI01);
		    dead_pflame60--;
		    break;
		}
	    } else if (dead_pflame60 == 0) {
		gameStatus = 2;
	    }
	    break;
	}
	p.view.toFront();
    }

    public void collisionNPC() {
	// NPCの周りの敵情報を探索
	Enemy[] aroundNPC = {
	    searchEnemy(npc.posX + 1, npc.posY), // 右
	    searchEnemy(npc.posX, npc.posY - 1), // 左
	    searchEnemy(npc.posX - 1, npc.posY), // 上
	    searchEnemy(npc.posX, npc.posY + 1) // 下
	};

	if (npc.collision(goal, 5) && goal.posY == npc.posY) {
	    gameStatus = 1;
	    goal.view.setImage(goalImage02);
	    return;
	}

	for (int i = 0; i < 4; i++) {
	    if (npc.collision(aroundNPC[i], 20)) {// NPC周りの敵
		gameStatus = 2;
		return;
	    }
	}
    }

    public void collisionPlayer() {
	// Player周りの敵情報を探索
	Enemy[] aroundPlayer = {
	    searchEnemy(p.posX + 1, p.posY), // 右
	    searchEnemy(p.posX, p.posY - 1), // 左
	    searchEnemy(p.posX - 1, p.posY), // 上
	    searchEnemy(p.posX, p.posY + 1) // 下
	};
	for (int i = 0; i < 4; i++) {
	    if (p.collision(aroundPlayer[i], 10)) {
		p.setHP(p.getHP() - 1);
		p.setStatus("FLASH");
		aroundPlayer[i].setDirection(p.getDirection()); // 反転
		aroundPlayer[i].move(120);
		aroundPlayer[i].setDirection(-1);

		if (p.getHP() <= 0)
		    p.setStatus("DEAD");
		return;
	    }
	}
	if (p.collision(heart, 20) && p.posY == heart.posY) {
	    p.setHP(p.getHP() + 1);
	    heart.view.setImage(new Image("./invisible.png"));
	    heart = null;
	}
    }

    public void drawHP() {
	for (int i = 0; i < HP; i++) {
	    Life tmp = totalLife.get(i);
	    if (i < p.getHP()) {
		tmp.turnOn();
	    } else {
		tmp.turnOff();
	    }
	    tmp.view.setTranslateX(400 + i * 30 + npc.getNowX() + 20 - initialSceneWidth / 2);
	    tmp.view.setTranslateY(50);
	}
    }

    public void openMap() {
	for (int i = 1; i <= MY; i++) {
	    for (int j = 1; j <= MX; j++) {
		System.out.print(map[i][j]);
	    }
	    System.out.println("");
	}
    }

    /* マス座標から該当する敵を取得 */
    public Enemy searchEnemy(int x, int y) {
	for (int i = 0; i < enemyList.size(); i++) {
	    Enemy tmp = enemyList.get(i);
	    if (tmp == null) {
	    } else if (tmp.posY == y && tmp.posX == x) {
		tmp.view.setImage(new Image("./E1_eff.png"));
		return tmp;
	    }
	}
	return null;
    }

    public void drawBackGround(int t) {
	timeText.setTranslateX(40 + npc.getNowX() + 20 - initialSceneWidth / 2);
	timeText.setFill(Color.WHITE);
	timeText.setText(Integer.toString(t));
	drawHP();
    }

    public void drawResult(Stage st) {
	Group resultRoot = new Group();
	Scene resultScene = new Scene(resultRoot, 600, 500, Color.rgb(0, 0, 0));

	resultScene.setOnKeyPressed(this::keyPressed);

	st.setTitle("The Guard");
	st.setScene(resultScene);
	st.show();

	Text resultMessage = new Text();

	int killedEnemy = 0;

	for (int i = 0; i < enemyList.size(); i++) {
	    if (enemyList.get(i) == null) {
		killedEnemy++;
	    }
	}

	if (gameStatus == 1) { // clear
	    resultMessage = new Text(200, 70, "Game Clear!!");
	} else if (gameStatus == 2) { // over
	    resultMessage = new Text(200, 70, "Game Over...");
	}
	resultMessage.setFont(new Font("", 40));
	resultMessage.setFill(Color.RED);

	Text playerLife = new Text(120, 150, "残りHP: ");
	playerLife.setFill(Color.WHITE);
	playerLife.setFont(new Font("", 30));

	Text score = new Text(120, 250, "倒した敵の数:      x  " + killedEnemy);
	score.setFill(Color.WHITE);
	score.setFont(new Font("", 30));

	Text endMessage = new Text(120, 400, "ゲーム終了: ESCAPE");
	endMessage.setFill(Color.WHITE);
	endMessage.setFont(new Font("", 30));

	for (int i = 0; i < HP; i++) {
	    Life tmp = totalLife.get(i);
	    resultRoot.getChildren().add(tmp.view);
	    if (i < p.getHP()) {
		tmp.turnOn();
	    } else {
		tmp.turnOff();
	    }
	    tmp.view.setTranslateX(250 + i * 30);
	    tmp.view.setTranslateY(125);
	}

	ImageView dead_enemy = new ImageView(e_lI01);
	dead_enemy.setTranslateX(290);
	dead_enemy.setTranslateY(220);

	resultRoot.getChildren().addAll(resultMessage, playerLife, score, dead_enemy, endMessage);

	gameStatus = 3;
    }

    public void keyPressed(KeyEvent e) {
	KeyCode key = e.getCode();

	if (gameStatus == 3) {
	    switch (key) {
	    case ESCAPE:
		System.out.println("ゲームを終了します。");
		System.exit(0);
		break;
	    default:
		break;
	    }
	}
    }

}

class ObjectView {
    protected ImageView view;// 表示させるキャラクターの画像
    protected char[][] map, field; // map全体
    protected int posX, posY; // マス座標

    private String status = "NEUTRAL"; // オブジェクトの状態を表す
    private int direction = 0; // オブジェクトの向きを表す

    protected char icon;

    ObjectView(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	this.map = map;
	this.field = field;
	this.icon = icon;
	this.posX = x;
	this.posY = y;
	view = new ImageView(img);
	view.setX(40 * x);
	view.setY(40 * y);
    }

    public ImageView getCharView() {
	return this.view;
    }

    public String getStatus() {
	return this.status;
    }

    public void setStatus(String s) {
	this.status = s;
    }

    public int getDirection() {
	return this.direction;
    }

    public void setDirection(int dir) {
	if (dir == -1) {
	    switch (this.direction) {
	    case 0:
		this.direction = 2;
		break;

	    case 2:
		this.direction = 0;
		break;
	    }
	} else {
	    this.direction = dir;
	}
    }

    public double getNowX() {
	return view.getX();
    }

    public double getNowY() {
	return view.getY();
    }

    /* オブジェクト同士の衝突を判定（縦横上下） */
    public boolean collision(ObjectView o, double px) {
	if (o == null)
	    return false;

	double ax = this.getNowX();
	double ay = this.getNowY();
	double bx = o.getNowX();
	double by = o.getNowX();

	if (-px <= ax - bx && ax - bx <= px)
	    return true;
	else if (-px <= bx - ax && bx - ax <= px)
	    return true;
	else if (-px <= ay - by && ay - by <= px)
	    return true;
	else if (-px <= by - ay && by - ay <= px)
	    return true;
	else
	    return false;
    }

    /* nマス以内に対象としたオブジェクトがあるかどうか判定 */
    public boolean noticeObject(int n, ObjectView o) {
	return this.posY == o.posY && (-1 * n) <= o.posX - this.posX && o.posX - this.posX <= n;
    }
}

/* Itemクラス(ObjectView継承) */
class Item extends ObjectView {
    public Item(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	super(x, y, img, map, field, icon);
    }
}

/* Actionクラス(ObjectView継承) */
class Action extends ObjectView {

    public Action(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	super(x, y, img, map, field, icon);
    }

    public void moveOnMap() {
	if ((posX + 1) * 40 < view.getX()) {
	    map[posY][posX] = ' '; // map上で移動を表現
	    this.posX++;
	    map[posY][posX] = icon;
	    //System.out.println(icon + "x: " + this.posX);
	} else if (view.getX() < (posX - 1) * 40) {
	    map[posY][posX] = ' '; //
	    this.posX--;
	    map[posY][posX] = icon;
	    //System.out.println(icon + "x: " + this.posX);
	} else if (view.getY() < (posY - 1) * 40) {
	    map[posY][posX] = ' ';
	    this.posY--;
	    map[posY][posX] = icon;
	    //System.out.println(icon + "y: " + this.posY);
	} else if ((posY + 1) * 40 <= view.getY()) {
	    map[posY][posX] = ' ';
	    this.posY++;
	    map[posY][posX] = icon;
	    //System.out.println(icon + "y: " + this.posY);
	}
    }

    public void move(double v) {
	moveOnMap();
	switch (super.getDirection()) {
	case 0: // right
	    if (field[posY][posX + 1] != 'B') {
		view.setX(view.getX() + v); // 画像をvピクセル移動
	    }
	    break;
	case 1: // up
	    if (field[posY][posX] == 'L') {
		view.setY(view.getY() - v);
	    }
	    break;
	case 2: // left
	    if (field[posY][posX - 1] != 'B') {
		view.setX(view.getX() - v);
	    }
	    break;
	case 3: // down
	    if (field[posY + 1][posX] == 'L') {
		view.setY(view.getY() + v);
	    }
	    break;
	}
    }

    public void fall() {
	moveOnMap();
	if (field[posY + 1][posX] != 'B') {
	    view.setY(view.getY() + 2); // 定数2
	}
    }

    public void jump() {
	moveOnMap();
	if (field[posY - 1][posX] != 'B') {
	    view.setY(view.getY() - 2); // 定数2
	}
    }
}

/* Enemyクラス(Action継承) */
class Enemy extends Action {
    Enemy(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	super(x, y, img, map, field, icon);
    }
}

/* NPCクラス(Action継承) */
class NPC extends Action {
    NPC(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	super(x, y, img, map, field, icon);
    }
}

/* Playerクラス(Action継承) */
class Player extends Action {
    int hp;

    Player(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	super(x, y, img, map, field, icon);
    }

    public void attack(Enemy e) {
	if (e == null) {
	} else {
	    e.setStatus("DEAD");
	}
	return;
    }

    public int getHP() {
	return this.hp;
    }

    public void setHP(int i) {
	this.hp = i;
    }
}

/* Goalクラス(Action継承) */
class Goal extends ObjectView {
    Goal(int x, int y, Image img, char[][] map, char[][] field, char icon) {
	super(x, y, img, map, field, icon);
	this.view.setY(40 * (y - 1));
    }
}

/* Lifeクラス */
class Life {
    Image life_off = new Image("./life_off.png");
    Image life_on = new Image("./life_on.png");
    ImageView view;

    Life() {
	view = new ImageView(life_on);
    }

    public void turnOn() {
	this.view.setImage(life_on);
    }

    public void turnOff() {
	this.view.setImage(life_off);
    }
}
