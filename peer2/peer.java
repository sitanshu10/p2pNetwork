import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class peer {

    public static Dictionary peersPeer2 = new Hashtable();
    public static int peerNumber = 1;

    public static String getFiles() {
        File directory = new File(".");
        File[] filesList = directory.listFiles();
        String directoryString = "";
        for(File f: filesList) {
            if(f.getName().contains("part")) {
                directoryString += f.getName() + "\n";
            }
        }
        return(directoryString.substring(0, directoryString.length() - 1));
    }

    public static boolean hasAllFiles() {
        String files = getFiles();
        if(files.contains("part1") && files.contains("part2") && files.contains("part3") && files.contains("part4") && files.contains("part5") && files.contains("part6") && files.contains("part7") && files.contains("part8") && files.contains("part9") && files.contains("part10")) {
            return true;
        }
        return false;
    }

    public static void mergeAllParts() {
        String FILE_NAME = "JohnWick.mp4";
        File ofile = new File(FILE_NAME);
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		List<File> list = new ArrayList<File>();
        for(int i = 1; i <= 10; i ++) {
            list.add(new File(FILE_NAME + ".part" + i));
        }
		try {
		    fos = new FileOutputStream(ofile,true);
		    for (File file : list) {
		        fis = new FileInputStream(file);
		        fileBytes = new byte[(int) file.length()];
		        bytesRead = fis.read(fileBytes, 0,(int)  file.length());
		        assert(bytesRead == fileBytes.length);
		        assert(bytesRead == (int) file.length());
		        fos.write(fileBytes);
		        fos.flush();
		        fileBytes = null;
		        fis.close();
		        fis = null;
		    }
		    fos.close();
		    fos = null;
		}catch (Exception exception){
			exception.printStackTrace();
		}
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7002);
        try {
            InetAddress inetIp = InetAddress.getByName("localhost");
            Socket socket = new Socket(inetIp, 5056);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            Scanner sc = new Scanner(System.in);
            while(true) {
                System.out.println(dataInputStream.readUTF());
                String toSend = sc.nextLine();
                dataOutputStream.writeUTF(toSend);
                if (toSend.equals("Exit")) {
                    System.out.println("Closing this connection.");
                    socket.close();
                    System.out.println("Connection Closed.");
                    break;
                }
                String recieved = dataInputStream.readUTF();
                System.out.println(recieved);
            }
            //sc.close();
            dataInputStream.close();
            dataOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread1 = new Thread () {
            public void run () {
                try {
                    Socket socketPeer2Client = new Socket(InetAddress.getByName("localhost"), 7003);
                    DataInputStream dataInputStreamPeer2Client = new DataInputStream(socketPeer2Client.getInputStream());
                    DataOutputStream dataOutputStreamPeer2Client = new DataOutputStream(socketPeer2Client.getOutputStream());
                    while(true) {
                        String clientFiles = getFiles();
                        String serverFiles = dataInputStreamPeer2Client.readUTF();
                        String[] serverFileList = serverFiles.split("\n");
                        if(hasAllFiles()) {
                            dataOutputStreamPeer2Client.writeUTF("Exit");
                            System.out.println("Receieved all parts.\nClosing this connection.");
                            mergeAllParts();
                            socketPeer2Client.close();
                            System.out.println("Connection Closed.");
                            break;
                        }
                        for(String fileName: serverFileList) {
                            if(!clientFiles.contains(fileName)) {
                                System.out.println("Requesting for file " + fileName + " from Peer3");
                                dataOutputStreamPeer2Client.writeUTF("Sending file " + fileName + " to Peer2");
                                String source = "../peer3/" + fileName;
                                File destination = new File(fileName);
                                Files.copy(Paths.get(source), destination.toPath());
                                break;
                            }
                        }
                        System.out.println(dataInputStreamPeer2Client.readUTF());
                    }
                    dataInputStreamPeer2Client.close();
                    dataOutputStreamPeer2Client.close();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread2 = new Thread () {
            public void run() {
                try {
                    //ServerSocket serverSocket = new ServerSocket(7002);
                    while(true) {
                        Socket socketPeer2Server = serverSocket.accept();
                        System.out.println("\nA new peer was connected.");
                        DataInputStream dataInputStreamPeer2Server = new DataInputStream(socketPeer2Server.getInputStream());
                        DataOutputStream dataOutputStreamPeer2Server = new DataOutputStream(socketPeer2Server.getOutputStream());
                        System.out.println("A new thread was allocated for this peer.");
                        peer.peersPeer2.put(socketPeer2Server.getPort(), "peer" + peerNumber);
                        peerNumber++;
                        Thread thread = new PeerHandler(socketPeer2Server, dataInputStreamPeer2Server, dataOutputStreamPeer2Server);
                        thread.start();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
        thread2.start();
    }

}

class PeerHandler extends Thread {

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket socket;
    Scanner sc = new Scanner(System.in);

    public PeerHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public static String getFiles() {
        File directory = new File(".");
        File[] filesList = directory.listFiles();
        String directoryString = "";
        for(File f: filesList) {
            if(f.getName().contains("part")) {
                directoryString += f.getName() + "\n";
            }
        }
        return(directoryString.substring(0, directoryString.length() - 1));
    }

    public void run() {
        String recieved, toReturn = "BOGUS", fileContent = "", filename;
        while(true) {
            try {
                String peerName = (String)peer.peersPeer2.get(socket.getPort());
                String serverFiles = getFiles();
                dataOutputStream.writeUTF(serverFiles);
                recieved = dataInputStream.readUTF();
                if (recieved.equals("Exit")) {
                    System.out.println((String)peer.peersPeer2.get(socket.getPort()) + " is exiting.");
                    this.socket.close();
                    socket.close();
                    System.out.println("Connection Closed.");
                    break;
                }
                else {
                    dataOutputStream.writeUTF("OOLALA");
                    System.out.println(recieved);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        try{
            this.dataInputStream.close();
            this.dataOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
