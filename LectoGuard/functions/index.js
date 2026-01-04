/**
 * Cloud Function para enviar notificaciones push de chat
 * 
 * Se ejecuta automáticamente cuando se crea un nuevo mensaje en una conversación
 */

const {onDocumentCreated} = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Se ejecuta automáticamente cuando se crea un nuevo mensaje en una conversación
 */
exports.sendChatNotification = onDocumentCreated(
  {
    region: 'us-central1',
    document: 'conversations/{conversationId}/messages/{messageId}'
  },
  async (event) => {
    try {
      const message = event.data.data();
      const conversationId = event.params.conversationId;
      const senderId = message.senderId;
      
      // Obtener la conversación para encontrar el otro participante
      const conversationDoc = await admin.firestore()
        .collection('conversations')
        .doc(conversationId)
        .get();
      
      if (!conversationDoc.exists) {
        console.log('Conversación no encontrada:', conversationId);
        return null;
      }
      
      const conversationData = conversationDoc.data();
      const participants = conversationData.participants || [];
      
      // Encontrar el destinatario (el que no es el remitente)
      const recipientId = participants.find((p) => p !== senderId);
      
      if (!recipientId) {
        console.log('No se encontró destinatario para la conversación:', conversationId);
        return null;
      }
      
      // Obtener el token FCM del destinatario
      const recipientProfile = await admin.firestore()
        .collection('users')
        .doc(recipientId)
        .get();
      
      if (!recipientProfile.exists) {
        console.log('Perfil del destinatario no encontrado:', recipientId);
        return null;
      }
      
      const recipientData = recipientProfile.data();
      const fcmToken = recipientData?.fcmToken;
      
      if (!fcmToken) {
        console.log('El destinatario no tiene token FCM:', recipientId);
        return null;
      }
      
      // Construir el payload de la notificación
      const payload = {
        notification: {
          title: message.senderName || 'Nuevo mensaje',
          body: message.content || ''
        },
        data: {
          type: 'chat_message',
          conversationId: conversationId,
          senderId: senderId,
          senderName: message.senderName || '',
          message: message.content || ''
        },
        token: fcmToken,
        android: {
          priority: 'high',
          notification: {
            sound: 'default'
          }
        },
        apns: {
          headers: {
            'apns-priority': '10'
          },
          payload: {
            aps: {
              sound: 'default'
            }
          }
        }
      };
      
      // Enviar la notificación
      const response = await admin.messaging().send(payload);
      console.log('Notificación enviada exitosamente:', response);
      
      return response;
    } catch (error) {
      console.error('Error enviando notificación:', error);
      return null;
    }
  }
);
