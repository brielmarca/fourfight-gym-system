const whatsappNumberDisplay = "+351 923 304 078";
const whatsappNumberWaMe = "351923304078";
const defaultWhatsAppMessage = "Olá 4Four Fight Academy, quero saber mais sobre as inscrições.";

const whatsappUrl = `https://wa.me/${whatsappNumberWaMe}?text=${encodeURIComponent(defaultWhatsAppMessage)}`;

const whatsappAriaLabel = "Abrir conversa no WhatsApp com a 4Four Fight Academy";

export {
  whatsappNumberDisplay,
  whatsappNumberWaMe,
  defaultWhatsAppMessage,
  whatsappUrl,
  whatsappAriaLabel,
};
