const rows = [
  ['Registration is closed. The organization has reached its limit of 25 accounts.', 'Regjistrimi është mbyllur. Organizata ka arritur kufirin prej 25 llogarish.', 'Les inscriptions sont fermées. La limite de 25 comptes est atteinte.', 'Die Registrierung ist geschlossen. Das Limit von 25 Konten ist erreicht.'],
  ['Email or username', 'Email ose emri i përdoruesit', 'E-mail ou nom d’utilisateur', 'E-Mail oder Benutzername'],
  ['Your username:', 'Emri juaj i përdoruesit:', 'Votre nom d’utilisateur :', 'Ihr Benutzername:'],
  ['Username:', 'Emri i përdoruesit:', 'Nom d’utilisateur :', 'Benutzername:'],
  ['Google account linked', 'Llogaria Google u lidh', 'Compte Google associé', 'Google-Konto verknüpft'],
  ['Link Google account', 'Lidh llogarinë Google', 'Associer un compte Google', 'Google-Konto verknüpfen'],
  ['Sign in with Google', 'Hyr me Google', 'Se connecter avec Google', 'Mit Google anmelden'],
  ['Google sign-in failed. Sign in with your password and link the matching Google account from your profile first.', 'Hyrja me Google dështoi. Hyni me fjalëkalim dhe lidhni llogarinë përkatëse Google nga profili juaj.', 'La connexion Google a échoué. Connectez-vous avec votre mot de passe, puis associez le compte Google correspondant depuis votre profil.', 'Google-Anmeldung fehlgeschlagen. Melden Sie sich mit Ihrem Passwort an und verknüpfen Sie zuerst das passende Google-Konto in Ihrem Profil.'],
  ['Invalid email or username, or password. If your username is shared, use your email.', 'Emaili, emri i përdoruesit ose fjalëkalimi është i pasaktë. Nëse emri i përdoruesit është i përbashkët, përdorni emailin.', 'Identifiants incorrects. Si votre nom d’utilisateur est partagé, utilisez votre e-mail.', 'Anmeldedaten ungültig. Bei gemeinsamem Benutzernamen verwenden Sie Ihre E-Mail.'],
];
export default Object.fromEntries(['sq', 'fr', 'de'].map((language, i) => [language, Object.fromEntries(rows.map(row => [row[0], row[i + 1]]))]));
