import java.util.*;

class Tougou {

    static final int SIZE = 5;
    static int[][] board = new int[SIZE][SIZE]; // 自軍盤面
    static int[][] board_enemy = new int[SIZE][SIZE]; // 敵軍盤面
    static int enemyResult = -1;// 相手攻撃の結果(秋山)
    static boolean moveBool = false;// 移動を前ターンでしたか(秋山)
    static int hitX, hitY, moveX, moveY;// 命中した座標と移動先の座標(秋山)

    // 巻き戻し用バックアップ変数(宮)
    static int[][] prevBoard = new int[SIZE][SIZE];
    static int[][] prevBoardEnemy = new int[SIZE][SIZE];
    static int prevEnemyResult;
    static boolean prevMoveBool;
    static int prevHitX, prevHitY, prevMoveX, prevMoveY;
    static int prevTurn;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 自軍・敵軍の盤面生成
        initBoard();
        setSubmarines();

        // 先攻・後攻入力
        System.out.print("先攻はどっち？: ");
        int turn = sc.nextInt(); // 0:自軍 1:敵軍
        System.out.println("");

        // ターンループ
        while (true) {

            // 巻き戻しメニューの追加 (宮)
            System.out.println("----- [ターン開始] -----");
            System.out.println("0: 行動を開始する");
            System.out.println("1: 1ターン巻き戻す (Undo)");
            int menuChoice = sc.nextInt();

            if (menuChoice == 1) {
                undo();

                continue;
            }
            // 行動前に現在の状態を保存(宮)
            saveState(turn);

            if (turn == 0) {
                // 自軍ターン
                // 移動 条件分岐追加(秋山)
                if (enemyResult == 1) {
                    System.out.println("自軍の移動");
                    selectMoveSbMrm(moveBool);
                    enemyResult = -1;
                    moveBool = true;
                } else {
                    System.out.println("自軍の攻撃:");
                    playerAttackAuto();
                    enemyResult = -1;
                    moveBool = false;
                }
                // playerAttackAuto();
                turn = 1; // 次は敵陣
            } else {
                // 敵軍ターン
                // 相手が移動する場合を追加
                System.out.println("敵軍の行動を選択してください (0:攻撃, 1:移動): ");
                int action = sc.nextInt();
                if (action == 0) {
                    System.out.println("敵軍の攻撃");
                    System.out.print("敵の攻撃位置(x y): ");
                    int ex = sc.nextInt();
                    int ey = sc.nextInt();

                    printAttackMessage(ex, ey);

                    enemyAttack(ex, ey);// 0 自軍撃沈、1 命中、2 波高し、3 ハズレ(秋山)
                    if (enemyResult == 1) {
                        hitX = ex;
                        hitY = ey;
                    } // 命中で生き残りの場合、座標記憶(秋山)

                } else {
                    // 移動の場合 (追加)
                    System.out.print("移動方向を入力 (N:北, S:南, W:西, E:東): ");
                    String dir = sc.next().toUpperCase();
                    System.out.print("移動マス数を入力 (1 or 2): ");
                    int dist = sc.nextInt();

                    updateEnemyExpectationForMove(dir, dist);
                    System.out.println("敵の移動（" + dir + "へ" + dist + "マス）を期待値盤面に反映しました。");
                }
                turn = 0; // 次は自軍
            }

            // 現在の状態表示
            printHP();
            printEnemy();

        }
    }

    // 現在の状態をコピーして保存(宮)
    static void saveState(int currentTurn) {
        for (int i = 0; i < SIZE; i++) {
            prevBoard[i] = board[i].clone();
            prevBoardEnemy[i] = board_enemy[i].clone();
        }
        prevEnemyResult = enemyResult;
        prevMoveBool = moveBool;
        prevHitX = hitX;
        prevHitY = hitY;
        prevMoveX = moveX;
        prevMoveY = moveY;
        prevTurn = currentTurn;
    }

    // 保存された状態を復元(宮)
    static void undo() {
        for (int i = 0; i < SIZE; i++) {
            board[i] = prevBoard[i].clone();
            board_enemy[i] = prevBoardEnemy[i].clone();
        }
        enemyResult = prevEnemyResult;
        moveBool = prevMoveBool;
        hitX = prevHitX;
        hitY = prevHitY;
        moveX = prevMoveX;
        moveY = prevMoveY;
        System.out.println("\n<<<< 1ターン巻き戻しました >>>>\n");
    }

    // 盤面をすべて0で初期化(濱口)
    static void initBoard() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = 0;
                board_enemy[x][y] = 0;
            }
        }
    }

    // 指定された初期位置に潜水艦を配置(濱口)
    static void setSubmarines() {
        int MaxAtckSell = 0;
        int    AtckSell = 0;
        int setX = -1, setY = -1;

        boolean boardB[][] = new boolean[SIZE][SIZE];
        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                boardB[i][j] = true; 
            }
        }
        // 端
        for(int i = 0; i < 3; i++){
            MaxAtckSell = 0;
            for(int y = 0; y < SIZE; y++){
            for(int x = 0; x < SIZE; x++){
                if(x == 0 || x == (SIZE-1) || y == 0 || y == (SIZE-1)){
                    AtckSell = 0;
                // 攻撃可能セル数カウント
                    for(int dy = -1; dy <= 1; dy++){
                    for(int dx = -1; dx <= 1; dx++){
                        if((x+dx) >= 0 && (x+dx) < SIZE && (y+dy) >= 0 && (y+dy) < SIZE && (dx != 0 && dy != 0)){
                            if(boardB[y+dy][x+dx]) AtckSell++;
                        }
                    }
                    }
                    if(MaxAtckSell < AtckSell){
                        MaxAtckSell = AtckSell;
                        setX = x;
                        setY = y;
                    }
                }
            }
            }

            for(int dy = -1; dy <= 1; dy++){
            for(int dx = -1; dx <= 1; dx++){
                if((setX + dx) >= 0 && (setX + dx) < SIZE && (setY + dy) >= 0 && (setY + dy) < SIZE){
                    boardB[setY + dy][setX + dx] = false;
                }
            }
            }
            board[setY][setX] = 3;
            System.out.println((char)('A'+ setY) + "-" + ( setX+1)+"    HP :3");
        }
        //not端
        MaxAtckSell = 0;
        for(int y = 0; y < SIZE; y++){
        for(int x = 0; x < SIZE; x++){        
                AtckSell = 0;
                // 攻撃可能セル数カウント
                for(int dy = -1; dy <= 1; dy++){
                for(int dx = -1; dx <= 1; dx++){
                    if((x+dx) >= 0 && (x+dx) < SIZE && (y+dy) >= 0 && (y+dy) < SIZE && (dx != 0 && dy != 0)){
                        if(boardB[y+dy][x+dx]) AtckSell++;
                    }
                }
                }
                if(MaxAtckSell < AtckSell){
                    MaxAtckSell = AtckSell;
                    setX = x;
                    setY = y;
                }
        }
        }
        board[setY][setX] = 3;
        System.out.println((char)('A'+ setY) + "-" + ( setX+1)+"    HP :3");
    }

    // 自軍潜水艦のHPのみ表示(濱口)
    static void printHP() {
        System.out.println("現在の自軍潜水艦のHP");

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] > 0) {
                    int colNum = y + 1;
                    // System.out.println("HP :" + board[x][y]);
                    System.out.println((char) ('A' + x) + "-" + (y + 1) + "    HP :" + board[x][y]);// 位置表示(秋山)
                }
            }
        }
        System.out.println(" ");
    }

    // 敵軍潜水艦期待値を盤面ごと表示(濱口)
    static void printEnemy() {
        System.out.println("現在の敵軍潜水艦位置の期待値");
        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        for (int x = 0; x < SIZE; x++) {
            System.out.print((char) ('A' + x) + " ");
            for (int y = 0; y < SIZE; y++) {
                System.out.print(board_enemy[x][y] + " ");
            }
            System.out.println();
        }
        System.out.println("");
    }

    // 敵軍が(x, y)に魚雷攻撃
    /*
     * //敵軍が(x, y)に魚雷攻撃
     * static void enemyAttack(int x, int y) {
     * if (board[x][y] > 0) {
     * board[x][y]--;
     * if (board[x][y] == 0) {
     * System.out.println("命中！撃沈！");
     * board[x][y] = -1;
     * } else {
     * System.out.println("命中！");
     * }
     * } else {
     * boolean ans = false; //波高し or ハズレをbooleanで管理
     * 
     * for (int dx = -1; dx <= 1; dx++) {
     * for (int dy = -1; dy <= 1; dy++) {
     * if (dx == 0 && dy == 0) continue;
     * int nx = x + dx;
     * int ny = y + dy;
     * if (!inRange(nx, ny)) continue;
     * if (board[nx][ny] > 0) ans = true;
     * }
     * }
     * System.out.println(ans ? "波高し！" : "ハズレ！");
     * }
     * 
     * //相手が攻撃したマスの周囲8マスを+1、そのマスを+０
     * board_enemy[x][y] = 0;
     * 
     * for (int dx = -1; dx <= 1; dx++) {
     * for (int dy = -1; dy <= 1; dy++) {
     * if (dx == 0 && dy == 0) continue;
     * int nx = x + dx;
     * int ny = y + dy;
     * if (!inRange(nx, ny)) continue;
     * if (board_enemy[nx][ny] >= 0) {
     * board_enemy[nx][ny] += 1;
     * }
     * }
     * }
     * }
     */

    // enemyResultに代入を追加(秋山)
    static void enemyAttack(int x, int y) {
        // 直撃の場合
        if (board[x][y] > 0) {
            board[x][y]--;
            if (board[x][y] == 0) {
                System.out.println("命中！撃沈！");
                board[x][y] = -1; // 撃沈マス
                enemyResult = 0;
            } else {
                System.out.println("命中！");
                enemyResult = 1;
            }
            System.out.println("");
            return;
        }

        // 波高しの場合
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dx++) {
                if (dx == 0 && dy == 0)
                    continue;

                int nx = x + dx;
                int ny = y + dy;

                if (!inRange(nx, ny))
                    continue;

                if (board[nx][ny] > 0) {
                    System.out.println("波高し！");
                    enemyResult = 2;
                    return;
                }
            }
        }
        System.out.println("ハズレ！");
        enemyResult = 3;

        // 相手が攻撃したマスの周囲8マスを+1、そのマスを0
        board_enemy[x][y] = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;

                int nx = x + dx;
                int ny = y + dy;

                if (!inRange(nx, ny))
                    continue;
                if (board_enemy[nx][ny] >= 0) {
                    board_enemy[nx][ny] += 1;
                }
            }
        }
    }

    // 自軍が (sx, sy) の潜水艦で (tx, ty) に攻撃
    static void playerAttack(int sx, int sy, int tx, int ty) {
        // 攻撃元チェック
        if (board[sx][sy] <= 0) {
            System.out.println("そこに自軍潜水艦はいません");
            return;
        }
        // 周囲8マスチェック
        if (!isAround8(sx, sy, tx, ty)) {
            System.out.println("その位置には攻撃できません");
            return;
        }

        // 攻撃宣言
        printAttackMessage(tx, ty);

        // 敵の応答
        String result = enemyResponse();
        System.out.println(result + "!");

        // 期待値更新
        if (result.equals("命中")) {
            // 攻撃した位置のマスを＋２する
            board_enemy[tx][ty] += 2;
        } else if (result.equals("波高し")) {
            // 攻撃した位置の周囲8マスを＋１する
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (!inRange(nx, ny))
                        continue;
                    board_enemy[nx][ny] += 1;
                }
            }
        } else if (result.equals("撃沈")) {
            // 撃沈した場合はその時に攻撃したマスを-100する
            board_enemy[tx][ty] = -100;
        }
    }

    // ヒューリスティック関数
    /*
     * 1. 自軍の潜水艦の攻撃できる周囲八マスの値をそれぞれ足し合わせて比較し、最も合計が大きい潜水艦を攻撃する潜水艦とする。ただし、
     * 最初は盤面の中心に最も近い潜水艦を選択する
     * 2．攻撃する潜水艦は周囲八マスで敵軍期待値が最も大きい箇所を選択し、攻撃する(ただし、最も大きい値が複数ある場合は盤面の中心に近いマスを選択する←
     * これは初期期待値の場合も同様)
     */

    static void playerAttackAuto() { // (濱口)
        int[] sub = selectSubmarine();
        int sx = sub[0];
        int sy = sub[1];

        int[] cell = selectAttackCell(sx, sy);
        int tx = cell[0];
        int ty = cell[1];

        System.out.println(
                "選択潜水艦: " + (char) ('A' + sx) + "-" + (sy + 1));
        playerAttack(sx, sy, tx, ty);
    }

    // 1. 攻撃する潜水艦を選択(濱口)
    static int[] selectSubmarine() {
        int bestX = -1, bestY = -1;
        int bestScore = -1;
        int bestDist = Integer.MAX_VALUE;

        boolean allZero = true;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board_enemy[x][y] != 0)
                    allZero = false;
            }
        }

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] <= 0)
                    continue;

                int score = sumAround8(x, y);
                int dist = centerDist(x, y);

                if (allZero) {
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestX = x;
                        bestY = y;
                    }
                } else {
                    if (score > bestScore ||
                            (score == bestScore && dist < bestDist)) {
                        bestScore = score;
                        bestDist = dist;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        return new int[] { bestX, bestY };
    }

    // 2. 攻撃する位置を選択(濱口)
    static int[] selectAttackCell(int sx, int sy) {
        int bestX = -1, bestY = -1;
        int bestScore = -1;
        int bestDist = Integer.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int nx = sx + dx;
                int ny = sy + dy;
                if (!inRange(nx, ny))
                    continue;

                int score = board_enemy[nx][ny];
                int dist = centerDist(nx, ny);

                if (score > bestScore ||
                        (score == bestScore && dist < bestDist)) {
                    bestScore = score;
                    bestDist = dist;
                    bestX = nx;
                    bestY = ny;
                }
            }
        }
        return new int[] { bestX, bestY };
    }

    // 周囲8マスの期待値を計算(濱口)
    static int sumAround8(int x, int y) {
        int sum = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int nx = x + dx;
                int ny = y + dy;
                if (!inRange(nx, ny))
                    continue;
                sum += board_enemy[nx][ny];
            }
        }
        return sum;
    }

    // 盤面中心の距離を測る(濱口)
    static int centerDist(int x, int y) {
        int c = SIZE / 2;
        return Math.abs(x - c) + Math.abs(y - c);
    }

    // 攻撃メッセージ表示(濱口)
    static void printAttackMessage(int x, int y) {
        char rowChar = (char) ('A' + x);
        int colNum = y + 1;
        System.out.println("[" + rowChar + "-" + colNum + "]に魚雷発射！");
    }

    // 防御ターン範囲チェック(濱口)
    static boolean inRange(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE; // 範囲以外のマスは攻撃できない
    }

    // 攻撃ターン攻撃チェック(濱口)
    static boolean isAround8(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty)
            return false;
        return Math.abs(sx - tx) <= 1 && Math.abs(sy - ty) <= 1;
    }

    // 敵軍からの攻撃結果を返す(濱口)
    static String enemyResponse() {
        Scanner sc = new Scanner(System.in);
        System.out.print("相手の応答:");
        String answer = sc.next();
        return answer;
    }

    // 命中させられた場合に逃げの 移動 (秋山)
    // 移動する潜水艦選択
    static void selectMoveSbMrm(boolean b) {
        if (!b) {
            Move(hitX, hitY);
        } else {
            int bestX = -1, bestY = -1;
            int bestScore = -1;
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if (board[x][y] <= 0)
                        continue;

                    int score = sumAround8(x, y);

                    if (score > bestScore && !(x == hitX && y == hitY) && !(x == moveX && y == moveY)) {
                        bestScore = score;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
            if (bestX != -1) {
                Move(bestX, bestY);
            } else {

                // 一応移動が不可能であった場合の処理
                System.out.println("自軍の攻撃:");
                playerAttackAuto();
                enemyResult = -1;
                moveBool = false;

            }
        }
    }

    // 移動先ぎめ
    static void Move(int x, int y) {
        int bestScore = -1;
        int bestX = -1, bestY = -1;
        String Compass = " ";

        // 宮くん発案の相手と重なる、を行いたいからスコアの高い座標へ移動
        // 北
        if (x > 0 && board[x - 1][y] == 0)
            if (board_enemy[x - 1][y] > bestScore) {
                bestScore = board_enemy[x - 1][y];
                bestX = x - 1;
                bestY = y;
                Compass = "北";
            }

        // 南
        if (x < 4 && board[x + 1][y] == 0)
            if (board_enemy[x + 1][y] > bestScore) {
                bestScore = board_enemy[x + 1][y];
                bestX = x + 1;
                bestY = y;
                Compass = "南";
            }

        // 西
        if (y > 0 && board[x][y - 1] == 0)
            if (board_enemy[x][y - 1] > bestScore) {
                bestScore = board_enemy[x][y - 1];
                bestX = x;
                bestY = y - 1;
                Compass = "西";
            }

        // 東
        if (y < 4 && board[x][y + 1] == 0)
            if (board_enemy[x][y + 1] > bestScore) {
                bestScore = board_enemy[x][y + 1];
                bestX = x;
                bestY = y + 1;
                Compass = "東";
            }

        // 入れ替え
        if (Compass != " ") {
            int r = board[x][y];
            board[x][y] = 0;
            board[bestX][bestY] = r;
            System.out.println(Compass + "へ 1 マス移動");
            moveX = bestX;
            moveY = bestY;
        }
        return;
    }

    // 期待値を相手の移動に合わせて移動
    static void updateEnemyExpectationForMove(String dir, int dist) {
        int[][] nextBoardEnemy = new int[SIZE][SIZE];
        int dx = 0, dy = 0;

        // 方向を座標の変化量に変換
        switch (dir) {
            case "N":
                dx = -1;
                break; // 北 (A方向)
            case "S":
                dx = 1;
                break; // 南 (E方向)
            case "W":
                dy = -1;
                break; // 西 (1方向)
            case "E":
                dy = 1;
                break; // 東 (5方向)
            default:
                System.out.println("無効な方向です。移動は反映されません。");
                return;
        }

        // 全てのマスの期待値を移動させる
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board_enemy[x][y] == 0)
                    continue;

                // 移動後の座標を計算
                int nx = x + (dx * dist);
                int ny = y + (dy * dist);

                // 移動後の座標が盤面内なら値を引き継ぐ
                if (inRange(nx, ny)) {
                    nextBoardEnemy[nx][ny] += board_enemy[x][y];
                }
                // 盤面外に出る場合は、その期待値は消滅（または端で止める処理も検討可）
            }
        }
        // 新しい盤面を現在の期待値盤面に上書き
        board_enemy = nextBoardEnemy;
    }

}
