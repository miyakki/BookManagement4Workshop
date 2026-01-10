import java.util.HashMap;
import java.util.Map;
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
        System.out.println("Please input a key");
        String key = sc.nextLine();
        if (hash.containsKey(key)) {
            System.out.println(key + "is already exists:" + hash.get(key));
        } else {
            System.out.println("Please input the value");
            int value = sc.nextInt();
            hash.put(key, value);
            
        }
    }

    private void saveHashToFile() {
       try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            
            for (Map.Entry<String, Integer> entry : hash.entrySet()) {
                String line = entry.getKey() + "," + entry.getValue();
                bw.write(line);
                bw.newLine(); 
            }
            
            bw.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        HashSave hs = new HashSave();
    }

}
cs25087@cs25087:/Windows/csprog$ java HashSave 
Please input a key
Bagu
Please input the value
10

