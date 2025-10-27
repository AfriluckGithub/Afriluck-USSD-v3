package org.gh.afriluck.afriluckussd.mapping;

import org.gh.afriluck.afriluckussd.constants.AppConstants;
import org.gh.afriluck.afriluckussd.dto.Transaction;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMapper.class);

    private final GameRepository gameRepository;

    public TransactionMapper(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Transaction mapTransactionFromSession(Session session, Game game, boolean wallet) {
        logger.info("Mapping transaction from session for MSISDN: {}, GameType: {}, Amount: {}",
                   session.getMsisdn(), session.getGameType(), session.getAmount());

        Transaction t = new Transaction();
        t.setGameId(session.getGameId());
        //t.setGame(session.isMorning()? "anopa": "657");
        t.setGame(session.isMorning() ? "anopa" : session.isAfternoon() ? "mid" : "657");
        String result = session.getGameType() == 1 ? "mega" : (session.getGameType() == 2 ? "direct" : "perm");
        t.setBetType(result);
        t.setSelectedNumbers(session.getSelectedNumbers());
        t.setEntryAmount(session.getAmount());
        t.setMsisdn(session.getMsisdn());
        t.setChannel(session.getNetwork());
        t.setTotalAmount(session.getAmount());
        int betTypeCode = result == "mega" ? 1 : session.getGameTypeCode();
        t.setBetTypeCode(betTypeCode);
        //t.setDrawCode(session.getGameTypeId());
        t.setDiscountedAmount(session.getDiscountedAmount());
        t.setMedium("ussd");
        t.setChannel(session.getNetwork());
        t.setUseWallet(wallet);
        t.setExtension(session.getExtension());

        logger.debug("Transaction mapped successfully: {}", t);
        return t;
    }


    public Transaction mapTransactionFromSessionPerm(Session session) {
        logger.info("Mapping perm transaction from session for MSISDN: {}, Amount: {}",
                   session.getMsisdn(), session.getAmount());

        Transaction t = new Transaction();
        //t.setGame(session.isMorning()? "anopa": "657");
        t.setGame(session.isMorning() ? "anopa" : session.isAfternoon() ? "mid" : "657");
        t.setGameId(session.getGameId());
        t.setBetType(AppConstants.PERM);
        t.setSelectedNumbers(session.getSelectedNumbers());
        t.setEntryAmount(session.getAmount());
        t.setMsisdn(session.getMsisdn());
        t.setChannel(session.getNetwork());
        t.setTotalAmount(session.getAmount());
        t.setBetTypeCode(session.getGameTypeCode());
        // t.setDrawCode(session.getGameTypeId());
        t.setDiscountedAmount(session.getDiscountedAmount());
        t.setExtension(session.getExtension());

        logger.debug("Perm transaction mapped successfully: {}", t);
        return t;
    }

    public Transaction mapTransactionFromSessionBanker(Session session, boolean wallet) {
        logger.info("Mapping banker transaction from session for MSISDN: {}, Amount: {}, Wallet: {}",
                   session.getMsisdn(), session.getAmount(), wallet);

        Transaction t = new Transaction();
        //t.setGame(session.isMorning()? "anopa": "657");
        t.setGame(session.isMorning() ? "anopa" : session.isAfternoon() ? "mid" : "657");
        t.setGameId(session.getGameId());
        t.setBetType(AppConstants.BANKER);
        t.setSelectedNumbers(session.getSelectedNumbers());
        t.setEntryAmount(session.getAmount());
        t.setMsisdn(session.getMsisdn());
        t.setChannel(session.getNetwork());
        t.setTotalAmount(session.getAmount());
        t.setBetTypeCode(2);
        // t.setDrawCode(session.getGameTypeId());
        t.setDiscountedAmount(session.getDiscountedAmount());
        t.setMedium("ussd");
        t.setChannel(session.getNetwork());
        t.setUseWallet(wallet);
        t.setExtension(session.getExtension());

        logger.debug("Banker transaction mapped successfully: {}", t);
        return t;
    }

    public Transaction mapPromo(String msisdn, String betType, String selectedNumbers, String medium, String channel) {
        logger.info("Mapping promo transaction for MSISDN: {}, BetType: {}", msisdn, betType);

        Transaction t = new Transaction();
        t.setMsisdn(msisdn);
        t.setBetType(betType);
        t.setSelectedNumbers(selectedNumbers);
        t.setMsisdn(msisdn);
        t.setMedium(medium);
        t.setChannel(channel);

        logger.debug("Promo transaction mapped successfully: {}", t.toString());
        return t;
    }

}
