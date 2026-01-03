import java.util.*;

class Test {
    
    static final int SIZE = 5;
    static Scanner sc = new Scanner(System.in);
    static int[][] board = new int[SIZE][SIZE]; //自軍盤面
    static int[][] board_enemy = new int[SIZE][SIZE];  //敵軍盤面
    static int[][] board_prev = new int[SIZE][SIZE];
    static int[][] board_enemy_prev = new int[SIZE][SIZE];
    static int turn_prev;

    public static void main(String[] args) {
        
    
        //自軍・敵軍の盤面生成
        initBoard();
        setSubmarines();
    
        //先攻・後攻入力
        System.out.print("先攻はどっち？: ");
        int turn = sc.nextInt(); //0:自軍 1:敵軍
        System.out.println("");
    
        //ターンループ 
        while (true) {
            
            saveState(turn);

            if (turn == 0) {
                //自軍ターン
                System.out.println("自軍の攻撃:");
                playerAttackAuto();
                turn = 1; //次は敵陣
            } else {
                //敵軍ターン 
                System.out.println("敵軍の攻撃");
                System.out.print("敵の攻撃位置(x y): ");
                int ex = sc.nextInt();
                int ey = sc.nextInt();

                // 座標が範囲外なら再入力させる
                if (!inRange(ex, ey)) {
                    System.out.println("エラー: 0〜4の範囲で入力してください。");
                    continue; 
                }
    
                printAttackMessage(ex, ey);
                enemyAttack(ex, ey);
                
                turn = 0; //次は自軍
            }
    
            //現在の状態表示
            printHP();
            printEnemy();
            System.out.print("今の入力を取り消して戻しますか？ (1:戻す / 0:進む): ");
            int choice = sc.nextInt();
            if (choice == 1) {
                turn = undo(); // 状態を復元
                System.out.println(">> ターンを戻しました。\n");
            // 現在の状態を再表示
                printHP();
                 printEnemy();
            }
    
        }
    }    

    //盤面をすべて0で初期化
    static void initBoard() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = 0;
                board_enemy[x][y] = 0;
            }
        }
    }

    //指定された初期位置に潜水艦を配置
    static void setSubmarines() {
        board[0][3] = 4; //A-4
        board[1][0] = 4; //B-1
        board[3][3] = 4; //D-4
        board[4][1] = 4; //E-2
    }

    //自軍潜水艦のHPのみ表示
    static void printHP() {
        System.out.println("現在の自軍潜水艦のHP");

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] > 0) {
                    int colNum = y + 1;
                    System.out.println("HP :" + board[x][y]);
                }
            }
        }
        System.out.println(" ");
    }

    //敵軍潜水艦期待値を盤面ごと表示
    static void printEnemy() {
        System.out.println("現在の敵軍潜水艦位置の期待値");
        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        for (int x = 0; x < SIZE; x++) {
            System.out.print((char)('A' + x) + " ");
            for (int y = 0; y < SIZE; y++) {
                System.out.print(board_enemy[x][y] + " ");
            }
            System.out.println();
        }
        System.out.println("");       
    }


    //敵軍が(x, y)に魚雷攻撃
    static void enemyAttack(int x, int y) {

        //直撃の場合
        if (board[x][y] > 0) {
            board[x][y]--;
            if (board[x][y] == 0) {
                System.out.println("命中！撃沈！");
                board[x][y] = -1; //撃沈マス
            } else {
                System.out.println("命中！");
            }
            System.out.println("");
            return;
        }

        //波高しの場合
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (!inRange(nx, ny)) continue; 

                if (board[nx][ny] > 0) {
                    System.out.println("波高し！");
                    return;
                }
            }
        }
        System.out.println("ハズレ！");
    }


    //自軍が (sx, sy) の潜水艦で (tx, ty) に攻撃
    static void playerAttack(int sx, int sy, int tx, int ty) {
        //攻撃元チェック
        if (board[sx][sy] <= 0) {
            System.out.println("そこに自軍潜水艦はいません");
            return;
        }
        //周囲8マスチェック
        if (!isAround8(sx, sy, tx, ty)) {
            System.out.println("その位置には攻撃できません");
            return;
        }

        //攻撃宣言
        printAttackMessage(tx, ty);

        //敵の応答
        String result = enemyResponse();
        /*System.out.println(result + "!");

        //期待値更新
        if (result.equals("1")) {
            //攻撃した位置のマスを＋２する
            board_enemy[tx][ty] = 1000;
        } else if (result.equals("2")) {
            //攻撃した位置の周囲8マスを＋１する
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (!inRange(nx, ny)) continue;
                    board_enemy[nx][ny] += 1;
                }
            }
        }else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0){
                        board_enemy[tx][ty] = -100;
                    }
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (!inRange(nx, ny)) continue;
                    board_enemy[nx][ny] = -1;
                }
            }
        }*/
       if (result.contains("1")) { // "命中"
            board_enemy[tx][ty] += 5; // 命中したマスは強くマーク
            System.out.println(">>> 命中を確認！");
        } 
        else if (result.contains("2")) { // "波高し"
            System.out.println(">>> 波高しを確認！周囲を調査します。");
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (inRange(nx, ny)) board_enemy[nx][ny] += 1;
                }
            }
        } 
        else { // "ハズレ"
            System.out.println(">>> ハズレ。このエリアには敵がいません。");
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (inRange(nx, ny)) {
                        // 攻撃した中心は大きくマイナス、周囲もマイナスにして二度と撃たないようにする
                        if (dx == 0 && dy == 0) board_enemy[nx][ny] = -10;
                        else board_enemy[nx][ny] = -1;
                    }
                }
            }
        }
    }


    //ヒューリスティック関数
    /*
    1. 自軍の潜水艦の攻撃できる周囲八マスの値をそれぞれ足し合わせて比較し、最も合計が大きい潜水艦を攻撃する潜水艦とする。ただし、最初は盤面の中心に最も近い潜水艦を選択する
    2．攻撃する潜水艦は周囲八マスで敵軍期待値が最も大きい箇所を選択し、攻撃する(ただし、最も大きい値が複数ある場合は盤面の中心に近いマスを選択する←これは初期期待値の場合も同様)
    */


    static void playerAttackAuto() {
        int[] sub = selectSubmarine();
        if (sub[0] == -1) {
        System.out.println("警告: 攻撃可能な潜水艦が見つかりません。");
        return;
        }
        int sx = sub[0];
        int sy = sub[1];

        int[] cell = selectAttackCell(sx, sy);
        
    
    // 攻撃先も見つからない場合の安全装置
        if (cell[0] == -1) {
            System.out.println("警告: 有効な攻撃先が見つかりません。");
            return;
    }
        
        int tx = cell[0];
        int ty = cell[1];

        System.out.println(
            "選択潜水艦: " + (char)('A'+sx) + "-" + (sy+1)
        );
        playerAttack(sx, sy, tx, ty);
    }

    //1. 攻撃する潜水艦を選択
    static int[] selectSubmarine() {
        int bestX = -1, bestY = -1;
        /*int bestScore = -1;
        int bestDist = Integer.MAX_VALUE;

        boolean allZero = true;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board_enemy[x][y] != 0) allZero = false;
            }
        }

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] <= 0) continue;

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
        }*/
        int bestScore = Integer.MIN_VALUE; 
        int bestDist = 100;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y] <= 0) continue;
                int score = sumAround8(x, y);
                int dist = centerDist(x, y);
            // これでマイナス同士の比較でも、大きい方が選ばれる
                if (score > bestScore || (score == bestScore && dist < bestDist)) {
                    bestScore = score;
                    bestDist = dist;
                    bestX = x;
                    bestY = y;
                }
            }
        }
    
    return new int[]{bestX, bestY};
    
    }

    //2. 攻撃する位置を選択
    static int[] selectAttackCell(int sx, int sy) {
        int bestX = -1, bestY = -1;
        int bestScore = -100;
        int bestDist = Integer.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = sx + dx;
                int ny = sy + dy;
                if (!inRange(nx, ny)) continue;

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
        return new int[]{bestX, bestY};
    }

    //周囲8マスの期待値を計算
    static int sumAround8(int x, int y) {
        int sum = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (!inRange(nx, ny)) continue;
                sum += board_enemy[nx][ny];
            }
        }
        return sum;
    }
    //盤面中心の距離を測る
    static int centerDist(int x, int y) {
        int c = SIZE / 2;
        return Math.abs(x - c) + Math.abs(y - c);
    }

    //攻撃メッセージ表示
    static void printAttackMessage(int x, int y) {
        char rowChar = (char)('A' + x);
        int colNum = y + 1;
        System.out.println("[" + rowChar + "-" + colNum + "]に魚雷発射！");
    }

    //防御ターン範囲チェック
    static boolean inRange(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE; //範囲以外のマスは攻撃できない
    }

    //攻撃ターン攻撃チェック
    static boolean isAround8(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return false;
        return Math.abs(sx - tx) <= 1 && Math.abs(sy - ty) <= 1;
    }

    //敵軍からの攻撃結果を返す
    static String enemyResponse() {
        
        System.out.print("相手の応答:");
        String answer = sc.next();
        return answer;
    }  
        // --- 配列をコピーして保存するメソッド ---
    static void saveState(int currentTurn) {
        turn_prev = currentTurn;
        for (int i = 0; i < SIZE; i++) {
            board_prev[i] = board[i].clone();
            board_enemy_prev[i] = board_enemy[i].clone();
        }
    }

// --- 保存した状態を復元するメソッド ---
    static int undo() {
        for (int i = 0; i < SIZE; i++) {
            board[i] = board_prev[i].clone();
            board_enemy[i] = board_enemy_prev[i].clone();
        }
        return turn_prev;
    }
}