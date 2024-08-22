package org.gh.afriluck.afriluckussd.mapping;

import org.gh.afriluck.afriluckussd.constants.AppConstants;
import org.gh.afriluck.afriluckussd.dto.Transaction;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    private final GameRepository gameRepository;

    public TransactionMapper(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Transaction mapTransactionFromSession(Session session, Game game, boolean wallet) {
        Transaction t = new Transaction();
        t.setGameId(session.getGameId());
        String result = session.getGameType() == 1 ? "mega" : (session.getGameType() == 2 ? "direct" : "perm");
        t.setBetType(result);
        t.setSelectedNumbers(session.getSelectedNumbers());
        t.setEntryAmount(session.getAmount());
        t.setMsisdn(session.getMsisdn());
        t.setChannel(session.getNetwork());
        t.setTotalAmount(session.getAmount());
        int betTypeCode = result == "mega" ? 1 : session.getGameTypeCode();
        t.setBetTypeCode(betTypeCode);
        t.setDrawCode(session.getGameTypeId());
        t.setDiscountedAmount(session.getDiscountedAmount());
        t.setMedium("ussd");
        t.setChannel(session.getNetwork());
        t.setUseWallet(wallet);
        return t;
    }

    ;


    public Transaction mapTransactionFromSessionPerm(Session session) {
        Transaction t = new Transaction();
        t.setGameId(session.getGameId());
        t.setBetType(AppConstants.PERM);
        t.setSelectedNumbers(session.getSelectedNumbers());
        t.setEntryAmount(session.getAmount());
        t.setMsisdn(session.getMsisdn());
        t.setChannel(session.getNetwork());
        t.setTotalAmount(session.getAmount());
        t.setBetTypeCode(session.getGameTypeCode());
        t.setDrawCode(session.getGameTypeId());
        t.setDiscountedAmount(session.getDiscountedAmount());
        return t;
    }

    public Transaction mapTransactionFromSessionBanker(Session session, boolean wallet) {
        Transaction t = new Transaction();
        t.setGameId(session.getGameId());
        t.setBetType(AppConstants.BANKER);
        t.setSelectedNumbers(session.getSelectedNumbers());
        t.setEntryAmount(session.getAmount());
        t.setMsisdn(session.getMsisdn());
        t.setChannel(session.getNetwork());
        t.setTotalAmount(session.getAmount());
        t.setBetTypeCode(2);
        t.setDrawCode(session.getGameTypeId());
        t.setDiscountedAmount(session.getDiscountedAmount());
        t.setMedium("ussd");
        t.setChannel(session.getNetwork());
        t.setUseWallet(wallet);
        return t;
    }

}
