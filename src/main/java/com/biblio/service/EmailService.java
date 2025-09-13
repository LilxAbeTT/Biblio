package com.biblio.service;

import com.biblio.model.dao.ConfiguracionDAO;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {
    private final ConfiguracionDAO configDao = new ConfiguracionDAO();

    public void enviar(String asunto, String cuerpo, String... destinatarios) throws Exception {
        String host = configDao.get("smtp.host", "");
        String port = configDao.get("smtp.port", "587");
        String user = configDao.get("smtp.user", "");
        String pass = configDao.get("smtp.pass", "");
        String from = configDao.get("smtp.from", user);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        for (String to : destinatarios) msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(asunto);
        msg.setText(cuerpo);
        Transport.send(msg);
    }
}
