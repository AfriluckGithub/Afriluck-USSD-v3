package org.gh.afriluck.afriluckussd.mapping;

import javax.annotation.processing.Generated;
import org.gh.afriluck.afriluckussd.dto.SessionDto;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-06-19T15:18:03+0000",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Homebrew)"
)
@Component
public class SessionMapperImpl implements SessionMapper {

    @Override
    public void updateSessionFromDto(SessionDto sessionDto, Session session) {
        if ( sessionDto == null ) {
            return;
        }

        if ( sessionDto.id != null ) {
            session.setId( sessionDto.id );
        }
        if ( sessionDto.network != null ) {
            session.setNetwork( sessionDto.network );
        }
        if ( sessionDto.msisdn != null ) {
            session.setMsisdn( sessionDto.msisdn );
        }
        if ( sessionDto.data != null ) {
            session.setData( sessionDto.data );
        }
        if ( sessionDto.gameType != null ) {
            session.setGameType( sessionDto.gameType );
        }
        if ( sessionDto.position != null ) {
            session.setPosition( sessionDto.position );
        }
        if ( sessionDto.selectedNumbers != null ) {
            session.setSelectedNumbers( sessionDto.selectedNumbers );
        }
        if ( sessionDto.amount != null ) {
            session.setAmount( sessionDto.amount );
        }
        if ( sessionDto.timeStamp != null ) {
            session.setTimeStamp( sessionDto.timeStamp );
        }
    }
}
