package codes.nh.webvideobrowser.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.Async;

public abstract class HttpServer {

    private final int TIMEOUT_CLIENT_CONNECT = 15000;

    private final int TIMEOUT_CLIENT_FULL = 25000;

    private final int port;

    private ServerSocket serverSocket;

    public HttpServer(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        if (serverSocket != null) {
            startListener.run();
            return;
        }
        AppUtils.log("starting server at port " + port);
        new Thread(() -> handleServer()).start();
    }

    public void stop() {
        if (serverSocket == null) return;
        AppUtils.log("stopping server");
        try {
            serverSocket.close();
        } catch (IOException e) {
            AppUtils.log("stop()", e);
        }
    }

    private int idCounter = 0;

    private void handleServer() {
        try (ServerSocket server = new ServerSocket(port);) {
            serverSocket = server;

            startListener.run();

            while (true) {

                Socket client = serverSocket.accept();
                client.setSoTimeout(TIMEOUT_CLIENT_CONNECT);
                //client.setKeepAlive(true);

                int id = idCounter;
                idCounter++;

                Async.execute(
                        () -> handleClient(client, id),
                        TIMEOUT_CLIENT_FULL
                );

            }

        } catch (Exception e) {
            AppUtils.log("handleServer()", e);
            serverSocket = null;

            stopListener.run();
        }
    }

    private void handleClient(Socket client, int id) {
        try (client;
             InputStream in = client.getInputStream();
             OutputStream out = client.getOutputStream();) {

            long startTime1 = System.currentTimeMillis();

            //AppUtils.log(id + " request from " + client.getInetAddress().toString());

            String firstLine;
            try {
                firstLine = readRequestFirstLine(in);
            } catch (Exception e) {
                AppUtils.log("read first line (" + id + ")", e);
                return;
            }

            AppUtils.log(id + " read first line took " + (System.currentTimeMillis() - startTime1));

            String[] requestLine = firstLine.split(" ", 3);
            String path = requestLine[1].substring(1);
            //AppUtils.log(id + " path is " + path);

            HttpResponse response = handleRequest(path, id);
            if (response == null) {
                AppUtils.log("response = null");
                return;
            }

            long startTime2 = System.currentTimeMillis();
            writeResponse(out, response);
            AppUtils.log(id + " writing took " + (System.currentTimeMillis() - startTime2));

            AppUtils.log(id + " done, whole thing took " + (System.currentTimeMillis() - startTime1));

            if (updateListener != null) updateListener.run();

        } catch (Exception e) {
            AppUtils.log("handleClient (" + id + ")", e);
        }
    }

    public abstract HttpResponse handleRequest(String path, int id) throws Exception;

    //read

    private String readRequestFirstLine(InputStream is) throws Exception {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String firstLine = br.readLine();
        if (firstLine == null) throw new Exception("request is empty");

        //need to read the whole request for some reason
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) break;
        }
        if (is.available() > 0) throw new Exception("request contains body");

        return firstLine;
    }


    //write

    private void writeResponse(OutputStream outputStream, HttpResponse response) throws Exception {

        if (response.length != null) {

            response.headers.clear(); //todo
            response.headers.put("Content-Length", String.valueOf(response.length));

        }

        response.headers.put("Access-Control-Allow-Origin", "*");

        StringBuilder headersBuilder = new StringBuilder();
        headersBuilder.append(response.status.getResponseLine()).append("\r\n");
        for (Map.Entry<String, String> header : response.headers.entrySet()) {
            headersBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        headersBuilder.append("\r\n");
        byte[] headers = headersBuilder.toString().getBytes();

        outputStream.write(headers);
        AppUtils.copyTo(response.content, outputStream);
        outputStream.flush();
    }

    //listeners

    private Runnable startListener = () -> {};

    private Runnable updateListener = () -> {};

    private Runnable stopListener = () -> {};

    public void setStartListener(Runnable startListener) {
        this.startListener = startListener;
    }

    public void setUpdateListener(Runnable updateListener) {
        this.updateListener = updateListener;
    }

    public void setStopListener(Runnable stopListener) {
        this.stopListener = stopListener;
    }

}
