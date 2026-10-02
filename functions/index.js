const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { Timestamp } = require("firebase-admin/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");


admin.initializeApp();

// Cloud Function che controlla i rinnovi ogni 24 ore
exports.controllaRinnovi = onSchedule({
    schedule: '0 0 * * *',
    timeZone: 'Europe/Rome'
}, async (event) => {
    const oggi = new Date(); // Data corrente
    const db = admin.firestore();

    try {
        const abbonamentiSnapshot = await db.collection("abbonamenti").get();

        for (const doc of abbonamentiSnapshot.docs) {
            const abbonamento = doc.data();
            const dataInizio = abbonamento.dataInizio.toDate(); // Converte il Timestamp in Date
            dataInizio.setDate(dataInizio.getDate() + 1); //questo perchè a causa del fusorario 
                                                          //torna indietro di un giorno quando si passa 
                                                          //da timestamp a date
            const dataFine = abbonamento.dataFine.toDate(); // Converte il Timestamp in Date
            dataFine.setDate(dataFine.getDate() + 1); //questo perchè a causa del fusorario 
                                                          //torna indietro di un giorno quando si passa 
                                                          //da timestamp a date
            var dataRinnovo = new Date(dataInizio);

            while (dataRinnovo.getFullYear() <= oggi.getFullYear() &&
            dataRinnovo.getMonth() <= oggi.getMonth() &&
            dataRinnovo.getFullYear() <= dataFine.getFullYear() &&
            dataRinnovo.getMonth() <= dataFine.getMonth()) {

                if (dataRinnovo.getFullYear() == oggi.getFullYear() &&
                dataRinnovo.getMonth() == oggi.getMonth() &&
                dataRinnovo.getDate() == oggi.getDate()) {
                    console.log(`Creazione transazione per l'abbonamento ${abbonamento.idAbbonamento}`);
                    await creaTransazione(abbonamento);
                    break; // Uscire dal ciclo una volta trovato il rinnovo
                }

                let giornoOriginale = dataRinnovo.getDate();
                let nextMonth = dataRinnovo.getMonth() + 1;
                let nextYear = dataRinnovo.getFullYear();

                // Se il mese è dicembre (11), passa a gennaio dell'anno successivo
                if (nextMonth > 11) {
                    nextMonth = 0;
                    nextYear += 1;
                }

                // Prova a impostare lo stesso giorno del mese successivo
                let nextRenewal = new Date(nextYear, nextMonth, giornoOriginale);

                // Se il giorno cambia, significa che il mese non ha quel giorno -> usa l'ultimo giorno del mese
                if (nextRenewal.getDate() !== giornoOriginale) {
                    nextRenewal = new Date(nextYear, nextMonth + 1, 0);
                }

                dataRinnovo = nextRenewal;                
                
                //console.log(`Nuovo rinnovo string ` + dataRinnovo.toLocaleDateString("it-IT"));
            }
        }
    } catch (error) {
        console.error("Errore durante il controllo dei rinnovi:", error);
    }
});

// Funzione per creare una nuova transazione
async function creaTransazione(abbonamento) {
    const db = admin.firestore();
    const id = getNewId("transazioni");

    const nuovaTransazione = {
        idUtente: abbonamento.idUtente,
        importo: abbonamento.importo,
        titolo: abbonamento.nome,
        idTransazione: id,
        idConto: abbonamento.conto,
        idCategoria: abbonamento.categoria,
        data: Timestamp.now(), // Usa Timestamp.now() per la data attuale
    };

    try {
        await db.collection("transazioni").doc(id).set(nuovaTransazione);
        const contoRef = db.collection("conti").doc(abbonamento.conto);
        const contoDoc = await contoRef.get();
        const saldoAttuale = contoDoc.data().saldo;
        const nuovoSaldo = saldoAttuale + abbonamento.importo;
        await contoRef.update({ saldo: nuovoSaldo });
    } catch (error) {
        console.error("Errore nella creazione della transazione:", error);
    }
}

// Funzione per generare un nuovo ID documento in Firestore
function getNewId(collectionName) {
    return admin.firestore().collection(collectionName).doc().id;
}