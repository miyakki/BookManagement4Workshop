import java.util.*;

class Miyatest {

    static final int SIZE = 5;
    static int[][] board = new int[SIZE][SIZE]; // 自軍盤面
    static int[][] board_enemy = new int[SIZE][SIZE]; // 敵軍盤面
    static int enemyResult = -1;// 相手攻撃の結果

    //↓(宮)
    static boolean moveBool = false;// 移動を前ターンでしたか
    static int hitX, hitY, moveX, moveY;// 命中した座標と移動先の座標
    // 状態管理用変数
    static int lastPlayerHitX = -1, lastPlayerHitY = -1;
    static int enemyMoveDx = 0, enemyMoveDy = 0;
    static boolean lastAttackWasHit = false;// 前ターンの自軍攻撃が命中したか
    static boolean enemyMovedLastTurn = false; // 敵が前ターンに「移動」したか

    // 文字列入力用（Uが入力されたらnullを返す）(宮)
    static String inputWithUndo(Scanner sc, String message) {
        System.out.print(message);
        String res = sc.next();
        if (res.equalsIgnoreCase("U")) {
            System.out.println(">> 入力を取り消してやり直します。");
            return null;
        }
        return res;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        initBoard();
        setSubmarines();

        // --- 先攻・後攻入力（エラーチェック付き） ---
        int turn = -1;
        while (turn != 0 && turn != 1) {
            System.out.print("先攻はどっち？ (0:自軍 1:敵軍): ");
            if (sc.hasNextInt()) {
                turn = sc.nextInt();
                if (turn != 0 && turn != 1)
                    System.out.println("0か1を入力してください。");
            } else {
                System.out.println("数値を入力してください。");
                sc.next(); // 不正な入力を捨てる
            }
        }
        System.out.println("");
        while (true) {
            if (turn == 0) {
                // 自軍ターン
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
                turn = 1;
            } else {
                // 敵軍ターン
                System.out.println("\n--------------------");
                System.out.println("【敵軍】のターン");

                // --- 行動選択（エラーチェック付き）(宮) ---
                int action = -1;
                while (action != 1 && action != 2) {
                    System.out.print("行動を選択 (1:攻撃 2:移動): ");
                    if (sc.hasNextInt()) {
                        action = sc.nextInt();
                        if (action != 1 && action != 2)
                            System.out.println("1か2を入力してください。");
                    } else {
                        System.out.println("数値を入力してください。");
                        sc.next();
                    }
                }

                if (action == 1) {
                    // --- 敵の攻撃位置入力（エラーチェック付き） ---
                    int ex = -1, ey = -1;
                    while (true) {
                        String ix = inputWithUndo(sc, "敵の攻撃位置 X座標(0-4) または Uで戻る: ");//(宮)
                        if (ix == null)
                            continue; // やり直し

                        try {
                            ex = Integer.parseInt(ix);

                            String iy = inputWithUndo(sc, "敵の攻撃位置 Y座標(0-4) または Uで戻る: ");//(宮)
                            if (iy == null)
                                continue; // やり直し
                            ey = Integer.parseInt(iy);

                            if (inRange(ex, ey)) {
                                break; // 正常な座標なら確定
                            }
                            System.out.println("座標は0～4の範囲で入力してください。");
                        } catch (NumberFormatException e) {
                            System.out.println("数値を入力してください。");
                        }
                    }
                    enemyAttack(ex, ey);
                    if (enemyResult == 1) {
                        hitX = ex;
                        hitY = ey;
                    }
                    enemyMoveDx = 0;
                    enemyMoveDy = 0;
                    enemyMovedLastTurn = false;
                } else {
                    processEnemyMove(sc);
                    enemyMovedLastTurn = true;
                }
                turn = 0;
            }
            printHP();
            printEnemy();
            if (isDefeated()) {
                System.out.println("自軍全滅。敗北です。");
                break;
            }
        }
    }

    // ★追加: 敗北判定メソッド
    static boolean isDefeated() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] > 0)
                    return false;
            }
        }
        return true;
    }

    static void initBoard() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = 0;
                board_enemy[x][y] = 0;
            }
        }
    }

    static void setSubmarines() {
        board[0][3] = 4; // A-4
        board[1][0] = 4; // B-1
        board[3][3] = 4; // D-4
        board[4][1] = 4; // E-2
    }

    static void printHP() {
        System.out.println("現在の自軍潜水艦のHP");
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] > 0) {
                    System.out.println((char) ('A' + x) + "-" + (y + 1) + "    HP :" + board[x][y]);
                }
            }
        }
        System.out.println(" ");
    }

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

    static void enemyAttack(int x, int y) {
        if (board[x][y] > 0) {
            board[x][y]--;
            if (board[x][y] == 0) {
                System.out.println("命中！撃沈！");
                board[x][y] = -1;
                enemyResult = 0;
            } else {
                System.out.println("命中！");
                enemyResult = 1;
            }
            return;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) { // ここdyじゃないですか？
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
    }

    static void playerAttack(int sx, int sy, int tx, int ty) {
        if (board[sx][sy] <= 0) {
            System.out.println("そこに自軍潜水艦はいません");
            return;
        }
        if (!isAround8(sx, sy, tx, ty)) {
            System.out.println("その位置には攻撃できません");
            return;
        }

        
        printAttackMessage(tx, ty);

        String result = enemyResponse();
        System.out.println(result + "!");

        if (result.equals("命中")) {
            board_enemy[tx][ty] += 2;
            lastPlayerHitX = tx;
            lastPlayerHitY = ty;//(宮)
            lastAttackWasHit = true;//(宮)
        } else if (result.equals("波高し")) {
            lastAttackWasHit = false;
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
        } else {
            lastAttackWasHit = false;
        }
    }

    // 相手の移動を入力＆期待値を相手の移動に合わせて変更（宮）
    static void processEnemyMove(Scanner sc) {
        String dir = "";
        int dist = -1;
        while (true) {
            dir = inputWithUndo(sc, "移動方向 (N, S, W, E) または Uで戻る: ");
            if (dir == null)
                continue;
            dir = dir.toUpperCase();
            if (!dir.matches("[NSWE]")) {
                System.out.println("N, S, W, E のいずれかを入力してください。");
                continue;
            }

            
            String idist = inputWithUndo(sc, "移動距離 (1 or 2) または Uで戻る: ");
            if (idist == null)
                    continue;
            try {
                dist = Integer.parseInt(idist);
                if (dist == 1 || dist == 2)
                    break; // 正常ならループを抜ける
                System.out.println("1か2を入力してください。");
            } catch (NumberFormatException e) {
                System.out.println("数値を入力してください。");
            }
        }

            enemyMoveDx = 0;
            enemyMoveDy = 0;
            if (dir.equals("N"))
                enemyMoveDx = -dist;
            else if (dir.equals("S"))
                enemyMoveDx = dist;
            else if (dir.equals("W"))
                enemyMoveDy = -dist;
            else if (dir.equals("E"))
                enemyMoveDy = dist;

            int[][] nextBoard = new int[SIZE][SIZE];
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if (board_enemy[x][y] > 0) {
                        int nx = x + enemyMoveDx;
                        int ny = y + enemyMoveDy;
                        if (inRange(nx, ny))
                            nextBoard[nx][ny] = board_enemy[x][y];
                    }
                }
            }
            board_enemy = nextBoard;
            System.out.println("敵が移動したため、期待値マップを更新しました。");
    }
    

    static void playerAttackAuto() {
        int[] sub = selectSubmarine();
        int sx = sub[0];
        int sy = sub[1];
        int[] cell = selectAttackCell(sx, sy);
        int tx = cell[0];
        int ty = cell[1];

        System.out.println("選択潜水艦: " + (char) ('A' + sx) + "-" + (sy + 1));
        playerAttack(sx, sy, tx, ty);
    }

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
                    if (score > bestScore || (score == bestScore && dist < bestDist)) {
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
    //攻撃の場合分けを実装(宮)
    static int[] selectAttackCell(int sx, int sy) {
        
        // 相手が移動した場合と、攻撃（居座り）した場合での場合分け
        if (enemyMovedLastTurn) {
            // 移動した場合かつ前回命中なら「追撃」
            if (lastAttackWasHit) {
                int targetX = lastPlayerHitX + enemyMoveDx;
                int targetY = lastPlayerHitY + enemyMoveDy;
                if (inRange(targetX, targetY) && isAround8(sx, sy, targetX, targetY)) {
                    System.out.println(">>> 移動先への追撃を行います。");
                    return new int[] { targetX, targetY };
                }
            }
        } else {
            // 攻撃（移動なし）の場合、盤面の最高期待値を狙う（前回の命中地点も含む）
            System.out.println(">>> 相手は動いていないため、高期待値の場所を狙います。");
        }

        // 通常の期待値ベースの選択
        int bestX = -1, bestY = -1, bestScore = -1, bestDist = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int nx = sx + dx, ny = sy + dy;
                if (!inRange(nx, ny))
                    continue;
                int score = board_enemy[nx][ny];
                int dist = centerDist(nx, ny);
                if (score > bestScore || (score == bestScore && dist < bestDist)) {
                    bestScore = score;
                    bestDist = dist;
                    bestX = nx;
                    bestY = ny;
                }
            }
        }
        return new int[] { bestX, bestY };
    }

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

    static int centerDist(int x, int y) {
        int c = SIZE / 2;
        return Math.abs(x - c) + Math.abs(y - c);
    }

    static void printAttackMessage(int x, int y) {
        char rowChar = (char) ('A' + x);
        int colNum = y + 1;
        System.out.println("[" + rowChar + "-" + colNum + "]に魚雷発射！");
    }

    static boolean inRange(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    static boolean isAround8(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty)
            return false;
        return Math.abs(sx - tx) <= 1 && Math.abs(sy - ty) <= 1;
    }

    static String enemyResponse() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String res = inputWithUndo(sc, "相手の応答 (命中/波高し/ハズレ / 修正は U): ");//(宮)
            if (res == null)
                continue; // Uが押されたら最初に戻る

            if (res.equals("命中") || res.equals("波高し") || res.equals("ハズレ")) {
                return res;
            }
            System.out.println("「命中」「波高し」「ハズレ」のいずれかを入力してください。");
        }
    }

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
            if (bestX != -1)
                Move(bestX, bestY);
            else {
                System.out.println("自軍の攻撃:");
                playerAttackAuto();
                enemyResult = -1;
                moveBool = false;
            }
        }
    }

    static void Move(int x, int y) {
        int bestScore = -1;
        int bestX = -1, bestY = -1;
        String Compass = " ";

        // 北
        if (x > 0 && board[x - 1][y] == 0) {
            if (board_enemy[x - 1][y] > bestScore) {
                bestScore = board_enemy[x - 1][y];
                bestX = x - 1;
                bestY = y;
                Compass = "北";
            }
        }

        // 南
        if (x < SIZE - 1 && board[x + 1][y] == 0) {
            if (board_enemy[x + 1][y] > bestScore) {
                bestScore = board_enemy[x + 1][y];
                bestX = x + 1;
                bestY = y;
                Compass = "南";
            }
        }

        // 西
        if (y > 0 && board[x][y - 1] == 0) {
            if (board_enemy[x][y - 1] > bestScore) {
                bestScore = board_enemy[x][y - 1];
                bestX = x;
                bestY = y - 1;
                Compass = "西";
            }
        }

        // 東
        if (y < SIZE - 1 && board[x][y + 1] == 0) {
            if (board_enemy[x][y + 1] > bestScore) {
                bestScore = board_enemy[x][y + 1];
                bestX = x;
                bestY = y + 1;
                Compass = "東";
            }
        }

        // 実際に移動を反映
        if (!Compass.equals(" ")) {
            int shipHP = board[x][y];
            board[x][y] = 0;
            board[bestX][bestY] = shipHP;
            System.out.println(Compass + "へ 1 マス移動");
            moveX = bestX;
            moveY = bestY;
        }
    }
}