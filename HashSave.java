import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class HashSave {

    private HashMap<String, Integer> hash = new HashMap<String,Integer>();
    private File f = new File("data.txt");

    public HashSave() {
        getFromFile();
        registerToHash();
        saveHashToFile();
    }

    private void getFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] sp_line = new String[2];
            while ((line = br.readLine()) != null) {
                sp_line = line.split(",");
                hash.put(sp_line[0], Integer.parseInt(sp_line[1]));
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void registerToHash() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the key");
        String line = sc.nextLine();
        if (hash.containsKey(key)) {
            System.out.println("そのキーは既に登録されています: " + key + " = " + hash.get(key));
        } else {
            // 未登録であれば値を入力させて登録
            System.out.println("Please input the value (Integer):");
            try {
                int value = Integer.parseInt(sc.nextLine());
                hash.put(key, value);
                System.out.println("登録しました。");
            } catch (NumberFormatException e) {
                System.out.println("数値の形式が正しくありません。登録を中止します。");
            }
        }
    }

    private void saveHashToFile() {
       try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            
            // HashMapの全ての要素をループで取り出し、ファイルに書き出す
            for (Map.Entry<String, Integer> entry : hash.entrySet()) {
                String line = entry.getKey() + "," + entry.getValue();
                bw.write(line);
                bw.newLine(); // 改行を追加
            }
            
            bw.close(); // ファイルを閉じる（ここで実際に書き込まれる）
            System.out.println("ファイルに保存しました。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        HashSave hs = new HashSave();
    }

}

