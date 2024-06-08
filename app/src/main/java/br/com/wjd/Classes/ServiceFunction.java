package br.com.wjd.Classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceFunction {

    public String sendPost(String url, String json, String Auth) throws MinhaException {

        try {

            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                request.setDoOutput(true);
                request.setDoInput(true);

                request.setRequestProperty("Content-Type", "application/json");
                if (!Auth.isEmpty())
                    request.setRequestProperty("Authorization", "Bearer "+Auth);

                request.setRequestMethod("POST");
                request.setReadTimeout(15000);

                request.connect();

                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                return readResponse(request);
            } finally {
                request.disconnect();
            }
        } catch (IOException ex) {
            throw new MinhaException(ex);
        }
    }

    private String readResponse(HttpURLConnection request) throws IOException {
        ByteArrayOutputStream os;
        try (InputStream is = request.getInputStream()) {
            os = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
        }
        return new String(os.toByteArray());
    }

    public static class MinhaException extends Exception {
        private static final long serialVersionUID = 1L;

        public MinhaException(Throwable cause) {
            super(cause);
        }
    }
}