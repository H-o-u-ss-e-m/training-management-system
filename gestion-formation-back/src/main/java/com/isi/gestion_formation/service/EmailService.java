package com.isi.gestion_formation.service;



import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@localhost}")
    private String from;

    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            if (to == null || to.isBlank()) return;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi email à " + to + " : " + e.getMessage());
        }
    }

    // -----------------------
    // PARTICIPANT INSCRIPTION
    // -----------------------
    public void sendInscriptionEmail(String to, String prenomNom,
                                     String titreFormation, String lieu,
                                     String dateFormation, Integer duree) {
        String subject = "Inscription confirmée – Formation : " + titreFormation;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 650px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;">
              <div style="background: #667eea; padding: 22px; text-align: center;">
                <h1 style="color: #fff; margin:0;">Confirmation d'inscription</h1>
              </div>
              <div style="padding: 28px;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Vous avez été ajouté(e) à la formation suivante :</p>

                <table style="width:100%%; border-collapse: collapse; margin-top: 16px;">
                  <tr style="background:#f6f7ff;">
                    <td style="padding:10px; font-weight:bold; width: 35%%;">Formation</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Lieu</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr style="background:#f6f7ff;">
                    <td style="padding:10px; font-weight:bold;">Date</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Durée</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                </table>

                <p style="margin-top:20px;">Cordialement,<br><strong>Excellent Training</strong></p>
              </div>
              <div style="background:#fafafa; padding: 14px; text-align:center; font-size:12px; color:#777;">
                Email automatique — merci de ne pas répondre.
              </div>
            </div>
            """.formatted(
                safe(prenomNom),
                safe(titreFormation),
                safe(lieu, "À définir"),
                safe(dateFormation, "À définir"),
                duree != null ? (duree + " jour(s)") : "N/A"
        );

        sendEmail(to, subject, html);
    }

    // -----------------------
    // FORMATEUR AFFECTATION
    // -----------------------
    public void sendFormateurAssignationEmail(String to, String prenomNomFormateur,
                                              String titreFormation, String lieu,
                                              String dateFormation, Integer duree,
                                              Double budget, int nbParticipants) {
        String subject = "Affectation à la formation : " + titreFormation;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 650px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;">
              <div style="background: #10b981; padding: 22px; text-align: center;">
                <h1 style="color: #fff; margin:0;">Affectation Formateur</h1>
              </div>
              <div style="padding: 28px;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Vous avez été affecté(e) en tant que formateur(trice) sur :</p>

                <table style="width:100%%; border-collapse: collapse; margin-top: 16px;">
                  <tr style="background:#f1fffb;">
                    <td style="padding:10px; font-weight:bold; width: 35%%;">Formation</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Lieu</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr style="background:#f1fffb;">
                    <td style="padding:10px; font-weight:bold;">Date</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Durée</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr style="background:#f1fffb;">
                    <td style="padding:10px; font-weight:bold;">Participants</td>
                    <td style="padding:10px;">%d</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Budget</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                </table>

                <p style="margin-top:20px;">Cordialement,<br><strong>Excellent Training</strong></p>
              </div>
              <div style="background:#fafafa; padding: 14px; text-align:center; font-size:12px; color:#777;">
                Email automatique — merci de ne pas répondre.
              </div>
            </div>
            """.formatted(
                safe(prenomNomFormateur),
                safe(titreFormation),
                safe(lieu, "À définir"),
                safe(dateFormation, "À définir"),
                duree != null ? (duree + " jour(s)") : "N/A",
                nbParticipants,
                budget != null ? (budget + " DT") : "À définir"
        );

        sendEmail(to, subject, html);
    }

    // -----------------------
    // PROPOSITION (OFFRE)
    // -----------------------
    public void sendPropositionEmail(String to, String prenomNom,
                                     String titreFormation, String lieu,
                                     String dateFormation, Integer duree,
                                     String domaineLibelle) {
        String subject = "Offre de formation — " + titreFormation;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 650px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;">
              <div style="background: #f59e0b; padding: 22px; text-align: center;">
                <h1 style="color: #fff; margin:0;">Proposition de formation</h1>
              </div>
              <div style="padding: 28px;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Nous vous proposons la formation suivante :</p>

                <div style="background:#fff7ed; border-left: 4px solid #f59e0b; padding: 14px; margin-top: 16px;">
                  <p style="margin:6px 0;"><strong>Titre:</strong> %s</p>
                  <p style="margin:6px 0;"><strong>Domaine:</strong> %s</p>
                  <p style="margin:6px 0;"><strong>Date:</strong> %s</p>
                  <p style="margin:6px 0;"><strong>Durée:</strong> %s</p>
                  <p style="margin:6px 0;"><strong>Lieu:</strong> %s</p>
                </div>

                <p style="margin-top:18px;">Si vous êtes intéressé(e), merci de contacter l'administration.</p>
                <p style="margin-top:20px;">Cordialement,<br><strong>Excellent Training</strong></p>
              </div>
            </div>
            """.formatted(
                safe(prenomNom),
                safe(titreFormation),
                safe(domaineLibelle, "N/A"),
                safe(dateFormation, "À définir"),
                duree != null ? (duree + " jour(s)") : "N/A",
                safe(lieu, "À définir")
        );

        sendEmail(to, subject, html);
    }

    // -----------------------
    // HISTORIQUE (a déjà participé)
    // -----------------------
    public void sendHistoriqueEmail(String to, String prenomNom,
                                    String titreFormation, String dateFormation,
                                    String lieu, String domaineLibelle) {
        String subject = "Historique — Participation : " + titreFormation;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 650px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;">
              <div style="background: #764ba2; padding: 22px; text-align: center;">
                <h1 style="color: #fff; margin:0;">Récapitulatif de participation</h1>
              </div>
              <div style="padding: 28px;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Nous vous confirmons votre participation à :</p>

                <table style="width:100%%; border-collapse: collapse; margin-top: 16px;">
                  <tr style="background:#f7f2ff;">
                    <td style="padding:10px; font-weight:bold; width: 35%%;">Formation</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Domaine</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr style="background:#f7f2ff;">
                    <td style="padding:10px; font-weight:bold;">Date</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px; font-weight:bold;">Lieu</td>
                    <td style="padding:10px;">%s</td>
                  </tr>
                </table>

                <p style="margin-top:20px;">Cordialement,<br><strong>Excellent Training</strong></p>
              </div>
            </div>
            """.formatted(
                safe(prenomNom),
                safe(titreFormation),
                safe(domaineLibelle, "N/A"),
                safe(dateFormation, "N/A"),
                safe(lieu, "N/A")
        );

        sendEmail(to, subject, html);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}