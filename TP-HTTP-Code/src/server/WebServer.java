///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

public class WebServer {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java http.server.WebServer <http.server.Server port>");
            System.exit(1);
        }
        WebServer ws = new WebServer();
        ws.start(args[0]);
    }

   private static final String RESOURCE_DIRECTORY = "resource";
   private static final String INDEX = RESOURCE_DIRECTORY + "/index.html";
   private static final String FILE_NOT_FOUND = RESOURCE_DIRECTORY + "/not-found.html";
    
                    
              
    public void start(String port) {
        ServerSocket s;


        System.out.println("Webserver starting up on port " + port);
        System.out.println("(press CTRL+C to exit)");
        try {
            //creation d'une socket serveur
            s = new ServerSocket(Integer.parseInt(port));
        } catch (Exception e) {
            //a modifier 
            //e.printStackTrace();
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (; ; ) {
            //creation d'une socket client 
            Socket clientSocket = null;
            BufferedInputStream in = null;
            BufferedOutputStream out = null;


            try {
                //la socket client va accepter la socket serveur
                clientSocket = s.accept();

                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream()); 
            
        


                List<String> headers = ExtractHeader(in);
                
              String filePath = new File("").getAbsolutePath();            
            filePath = filePath.concat("\\resource\\");
            filePath = filePath.concat("header.txt");
            FileWriter fw = new FileWriter(filePath,true);
            
                if (headers.isEmpty()) {
                  
                    out.write(writeHeader("400 Bad Request","header.txt"));
                    out.flush();
                } else {
                 
                   
                    StringTokenizer parse = new StringTokenizer(headers.get(0));
                    String methode = parse.nextToken().toLowerCase();
                    String resourceDemande = parse.nextToken().toLowerCase().substring(1);
                    
                   


                    if (resourceDemande.isEmpty()) {
                    
                    
                    RequeteGet(out, "index.html");
                    fw.write("Request Header : GET ");
                    fw.write("index.html ");
                    } else  {
                        switch (methode) {
                            
                            case "head":
                            
                             
                                RequeteHead(out, resourceDemande);
                                break;
                          
                            case "get":
                            
                             
                                RequeteGet(out, resourceDemande);
                                break;
                            
                            case "post":
                          
                                 StringTokenizer multiTokenizer = new StringTokenizer(resourceDemande, "/");
                                
                                    String resource=multiTokenizer.nextToken();
                                    String site=multiTokenizer.nextToken(); 
                                   
                               

                                RequetePost(in, out,resource); 
                                
                                break;
                            
                            case "put":
                            
                                RequetePut(in, out, resourceDemande);
                                break;
                            case "delete":
                            
                                RequeteDelete(out, resourceDemande);
                                break;
                            default:
                                out.write(writeHeader("501 Not Implemented","Aucune"));
                                out.flush();
                                break;
                        }
                    } 
                }

                clientSocket.close();

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    out.write(writeHeader("500 Internal Server Error","Aucune"));
                    out.flush();
                    clientSocket.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

  

/**  Cette methode verifie l'existence de la ressource, et retourne un en-tete qui contient les memes informations que la requete GET correspondante.
* Le code de retour peut etre 200 OK si le fichier a ete trouve, ou 404 Not Found si le fichier n'a pas ete trouve.
*@param out flux d'ecriture binaire vers le socket client .
*@param resourcedemandee Chemin du fichier dont le client veut vérifier l'existence
*/
    public void RequeteHead(BufferedOutputStream out, String resourceDemande) {
         String filePath = new File("").getAbsolutePath();
                    
                    filePath = filePath.concat("\\resource\\");
                   
                    filePath = filePath.concat(resourceDemande);
                    resourceDemande=filePath;
        System.out.println("HEAD " + resourceDemande);
        try {
            File file = new File(resourceDemande);
            if (file.exists() && file.isFile()) {
                out.write(writeHeader("200 OK", resourceDemande, file.length(),"HEAD"));
            } else {
                out.write(writeHeader("404 Not Found",resourceDemande,file.length(),"HEAD"));
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                out.write(writeHeader("500 Internal Server Error",resourceDemande));
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }

    
/** 
	 * ouvrir et de lire le fichier demande et de l'envoyer au client, sous forme de bytes.
	 * Le code de retour peut etre 200 OK si le fichier a ete trouve, ou 404 Not Found si le fichier n'a pas ete trouve.
	 * Si le fichier est trouve, la reponse contient un corps : le contenu du fichier demande envoye sous forme de bytes au browser.
	 * Si le fichier n'a pas ete trouve, la reponse contient aussi un corps : c'est le contenu du fichier designe par le chemin FILE_NOT_FOUND.
	 * @param out Flux d'ecriture binaire vers le socket client ou il faut ecrire la reponse.
	 * @param resourcedemandee Chemin du fichier que le client veut rcuperer.
	 */

    public void RequeteGet(BufferedOutputStream out, String resourceDemande) {
        System.out.println("GET " + resourceDemande);
         String filePath = new File("").getAbsolutePath();
                    
                    filePath = filePath.concat("\\resource\\");
                   
                    filePath = filePath.concat(resourceDemande);
                    resourceDemande=filePath;
                    
        try {
            File file = new File(resourceDemande);
            int fileLength = (int) file.length();
            if (file.exists() && file.isFile()) {
                out.write(writeHeader("200 OK", resourceDemande, fileLength ,"GET"));
            } else {
                file = new File(FILE_NOT_FOUND);
                fileLength = (int) file.length();
                out.write(writeHeader("404 Not Found", FILE_NOT_FOUND, fileLength,"GET" ));
            }
            byte[] fileData = LireDataDocument(file, fileLength);
            out.write(fileData, 0, fileLength);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                out.write(writeHeader("500 Internal Server Error",resourceDemande));
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }

 /**
	
	 * Cette methode envoie des données au serveur, les informations a envoyer sont les donnees du corps de la requete envoye.
	 * Si la ressource existait deja sur le serveur, les donnees sont ajoutees a la fin et l'en-tete de la reponse envoyee a un code de 200 OK.
	 * Si la ressource n'existait pas, elle est cree et l'en-tete a un code de 201 Created.
	 * les donnees du corps sont ecrites dans un document 
	 * @param in Flux de lecture binaire sur le socket client.
	 * @param out Flux d'ecriture binaire vers le socket client auquel il faut envoyer une reponse.
	 * @param filename Chemin du fichier danslequel il faut ecrire le corps.
	 */
    public void RequetePost(BufferedInputStream in, BufferedOutputStream out, String resourceDemande) {
       String filePath = new File("").getAbsolutePath();
                    
                    filePath = filePath.concat("\\resource\\");
                   
                    filePath = filePath.concat(resourceDemande);
                    resourceDemande=filePath;
                    
        System.out.println("POST " + resourceDemande);
        try {
            File file = new File(resourceDemande);
            int fileLength = (int) file.length();
            boolean fileExists = file.exists();

            List<Byte> fileData = new ArrayList<>();
            while (in.available() > 0) {
                fileData.add((byte) in.read());
            }

            byte[] fileDataArray = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                fileDataArray[i] = fileData.get(i);
            }
            writeFileData(file, fileDataArray);

            if (fileExists) {
                out.write(writeHeader("204 No Content",resourceDemande,fileLength,"POST"));
            } else {
                out.write(writeHeader("201 Created",resourceDemande,fileLength,"POST"));
            }
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.write(writeHeader("500 Internal Server Error",resourceDemande));
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }

    /**creer une nouvelle ressource et le contenu est constituee des donnees du corps de la request postman.
	 * Si une ressource du meme nom existe deja, elle est ecrasee et la reponse envoyee a un code de 204 No Content.
	 * Si la ressource n'existait pas, elle est cree et la reponse envoyee a un code de 201 Created.
     * @param in Flux de lecture binaire sur le socket client.
	 * @param out Flux d'ecriture binaire vers le socket client auquel il faut envoyer une reponse.
	 * @param filename Chemin du fichier danslequel il faut ecrire le corps.
      */

    public void RequetePut(BufferedInputStream in, BufferedOutputStream out, String resourceRequested) {
        System.out.println("PUT " + resourceRequested);
         String filePath = new File("").getAbsolutePath();
                    
                    filePath = filePath.concat("\\resource\\");
                   
                    filePath = filePath.concat(resourceRequested);
                    resourceRequested=filePath;
        try {
            File file = new File(resourceRequested);
            boolean fileExists = file.exists();
            int fileLength = (int) file.length();
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.close();

            List<Byte> fileData = new ArrayList<>();
            while (in.available() > 0) {
                fileData.add((byte) in.read());
            }

            byte[] fileDataArray = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                fileDataArray[i] = fileData.get(i);
            }
            writeFileData(file, fileDataArray);

            if (fileExists) {
                out.write(writeHeader("204 No Content",resourceRequested,fileLength,"PUT" ));
            } else {
                out.write(writeHeader("201 Created",resourceRequested,fileLength,"PUT" ));
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.write(writeHeader("500 Internal Server Error",resourceRequested));
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }
    /**  http delete pour supprimer une resource
    204 si la resource est bien supprimée
    404 si elle est pas trouvée
    403 si elle existe mais ne peut pas être supprimée
     * @param out Flux d'ecriture binaire vers le socket client auquel il faut envoyer une reponse.
	 * @param filename Chemin du fichier qu'il faut supprimer
     */
    public void RequeteDelete(BufferedOutputStream out, String resourceRequested) {
        System.out.println("DELETE " + resourceRequested);
         String filePath = new File("").getAbsolutePath();
                    
                    filePath = filePath.concat("\\resource\\");
                   
                    filePath = filePath.concat(resourceRequested);
                    resourceRequested=filePath;
        try {
            File file = new File(resourceRequested);
            boolean fileExists = file.exists();
            boolean fileDeleted = false;
            int fileLength = (int) file.length();
            if (fileExists && file.isFile()) {
                fileDeleted = file.delete();
            }

            if (fileDeleted) {
                out.write(writeHeader("204 No Content",resourceRequested,fileLength,"DELETE" ));
            } else if (!fileExists) {
                out.write(writeHeader("404 Not Found",resourceRequested,fileLength,"DELETE"  ));
            } else {
                out.write(writeHeader("403 Forbidden",resourceRequested,fileLength,"DELETE"  ));
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.write(writeHeader("500 Internal Server Error",resourceRequested));
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
        }
    }
   
      /**   Methode pour ecrire le header de la requete dans le flux de sortie 
     * @param status status code 204 ,404....selon l'etat du fichier
	 * @param filename Chemin du fichier demandee dans la requete
     */

    public byte[] writeHeader(String status,String resourceRequested) {
        String filePath = new File("").getAbsolutePath();            
        filePath = filePath.concat("\\resource\\");
        filePath = filePath.concat("header.txt");
        
        String header = "HTTP/1.0 " + status + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        //header +="ressource demandée :";
        //header += resourceRequested;
        System.out.println();
        System.out.println("Response header:");
        System.out.println(header);
        return header.getBytes();
    }

     /**   Methode pour ecrire le header de la requete dans le flux de sortie 
     * @param status status code 204 ,404....selon l'etat du fichier
	 * @param filename Chemin du fichier demandee dans la requete
     * @param lenght longueur du fichier
     * @param type type de la requete

     */

    public byte[] writeHeader(String status, String fileName, long length,String type) {
        String filePath = new File("").getAbsolutePath();            
        filePath = filePath.concat("\\resource\\");
        filePath = filePath.concat("header.txt");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        String header = "HTTP/1.0 " + status + "\r\n";
        header += "type de requete :" + type +"\n";
        if (fileName.endsWith(".txt")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: text/plain\r\n";
            header += dtf.format(now) +"\n";
        } else if (fileName.endsWith(".html")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: text/html\r\n";
            header += dtf.format(now)+"\n";
        } else if (fileName.endsWith(".png")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: image/png\r\n";
            header += dtf.format(now)+"\n";
        } else if (fileName.endsWith(".json")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: application/json\r\n";
            header += dtf.format(now)+"\n";
        } else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: image/jpeg\r\n";
            header += dtf.format(now)+"\n";
        } else if (fileName.endsWith(".mp3")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: audio/mp3\r\n";
            header += dtf.format(now)+"\n";
        } else if (fileName.endsWith(".mp4")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: video/mp4\r\n";
            header += dtf.format(now)+"\n";
        } else if (fileName.endsWith(".pdf")) {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: application/pdf\r\n";
            header += dtf.format(now)+"\n";
        } else {
            header +="ressource demandée :"+fileName+"\n";
            header += "Content-Type: application/octet-stream\r\n";
            header += dtf.format(now)+"\n";
        }

        header += "Content-Length: " + length + "\r\n";
      
        header += "\r\n";

        System.out.println();
        System.out.println("Response header: \n");
        System.out.println(header);
         try {
           
        FileWriter fw = new FileWriter(filePath,true);
        fw.write("Response Header :");
        fw.write(header);
         fw.close();
         }catch (Exception e) {
             e.printStackTrace();
         }

        return header.getBytes();
    }

     /**   Methode pour lire des donnees sous forme de byte dans un fichier
     * @param file  le fichier ou on souhaite lire les donnees.
     * @param fileLength la taille du fichier a lire
     */

    private byte[] LireDataDocument(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileIn != null) fileIn.close();
        }

        return fileData;
    }

     /**   Methode pour ecrire des donnees sous forme de byte dans un fichier
     * @param file  le fichier ou on souhaite ecrire les donnees.
     * @param fileData les donnees a ecrire sous forme de bytes dans le fichier 
     */

    private void writeFileData(File file, byte[] fileData) throws IOException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file, true);
            fileOut.write(fileData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) fileOut.close();
        }
    }
    /**   Methode pour extraire le header lors d'une requete
     * @param in Flux de lecture binaire sur le socket client.
	 

     */
      public List<String> ExtractHeader(BufferedInputStream in) throws IOException {
        int charRead = in.read();
        List<String> headers = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        while (charRead !=-1) {
            stringBuilder.append((char) charRead);
            if (stringBuilder.toString().endsWith("\r\n")) {
                if (stringBuilder.toString().equals("\r\n")) {
                    break;
                } else {
                    headers.add(stringBuilder.substring(0, stringBuilder.toString().lastIndexOf("\r\n")));
                    stringBuilder = new StringBuilder();
                }
            }
            charRead = in.read();
           
        }
        return headers;
    }


}