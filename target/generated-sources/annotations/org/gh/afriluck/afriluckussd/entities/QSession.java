package org.gh.afriluck.afriluckussd.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSession is a Querydsl query type for Session
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSession extends EntityPathBase<Session> {

    private static final long serialVersionUID = 204151549L;

    public static final QSession session = new QSession("session");

    public final NumberPath<Double> amount = createNumber("amount", Double.class);

    public final StringPath betTypeCode = createString("betTypeCode");

    public final DateTimePath<java.time.LocalDateTime> createdDate = createDateTime("createdDate", java.time.LocalDateTime.class);

    public final StringPath currentGame = createString("currentGame");

    public final StringPath data = createString("data");

    public final StringPath gameId = createString("gameId");

    public final NumberPath<Integer> gameType = createNumber("gameType", Integer.class);

    public final NumberPath<Integer> gameTypeCode = createNumber("gameTypeCode", Integer.class);

    public final StringPath gameTypeId = createString("gameTypeId");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath msisdn = createString("msisdn");

    public final StringPath network = createString("network");

    public final NumberPath<Integer> position = createNumber("position", Integer.class);

    public final StringPath selectedNumbers = createString("selectedNumbers");

    public final StringPath sequenceID = createString("sequenceID");

    public final StringPath timeStamp = createString("timeStamp");

    public final DateTimePath<java.time.LocalDateTime> updatedDate = createDateTime("updatedDate", java.time.LocalDateTime.class);

    public QSession(String variable) {
        super(Session.class, forVariable(variable));
    }

    public QSession(Path<? extends Session> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSession(PathMetadata metadata) {
        super(Session.class, metadata);
    }

}

