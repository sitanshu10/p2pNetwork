import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class splitFile{

    public static String FILE_NAME = "JohnWick.mp4";
    //private static byte PART_SIZE = 5;

    public static Dictionary peers = new Hashtable();
    public static int peerNumber = 1;

    public static void fileSplitter() {
        File inputFile = new File(FILE_NAME);
        FileInputStream inputStream;
        String newFileName;
        FileOutputStream filePart;
        int fileSize = (int) inputFile.length();
        int nChunks = 0, read = 0, readLength = fileSize/10;
        byte[] byteChunkPart;
        try {
            inputStream = new FileInputStream(inputFile);
            while (nChunks < 10) {
                byteChunkPart = new byte[readLength];
                read = inputStream.read(byteChunkPart, 0, readLength);
                fileSize -= read;
                assert (read == byteChunkPart.length);
                nChunks++;
                newFileName = FILE_NAME + ".part" + Integer.toString(nChunks);
                filePart = new FileOutputStream(new File(newFileName));
                filePart.write(byteChunkPart);
                filePart.flush();
                filePart.close();
                byteChunkPart = null;
                filePart = null;
            }
            //inputStream.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        fileSplitter();
        ServerSocket serverSocket = new ServerSocket(5056);
        while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("\nA new peer was connected.");
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("A new thread was allocated for this peer.");
            peers.put(socket.getPort(), "peer" + peerNumber);
            peerNumber++;
            Thread thread = new PeerHandler(socket, dataInputStream, dataOutputStream);
            thread.start();
        }
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

    public void run() {
        String recieved, toReturn = "BOGUS", fileContent = "", filename;
        while(true) {
            try {
                String peerName = (String)splitFile.peers.get(socket.getPort());
                dataOutputStream.writeUTF("\nEnter the file part numbers wanted.\n");
                recieved = dataInputStream.readUTF();
                if (recieved.equals("Exit")) {
                    System.out.println((String)splitFile.peers.get(socket.getPort()) + " is exiting.");
                    this.socket.close();
                    socket.close();
                    System.out.println("Connection Closed.");
                    break;
                }
                System.out.println("Sending the files: " + recieved + " to peer " + peerName);
                String[] partNumbers = recieved.split(",");
                for(int i = 0; i < partNumbers.length; i ++) {
                    int partNumber = Integer.parseInt(partNumbers[i]);
                    String source = splitFile.FILE_NAME + ".part" + partNumber;
                    File destination = new File("../" + peerName + "/" + source);
                    Files.copy(Paths.get(source), destination.toPath());
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
