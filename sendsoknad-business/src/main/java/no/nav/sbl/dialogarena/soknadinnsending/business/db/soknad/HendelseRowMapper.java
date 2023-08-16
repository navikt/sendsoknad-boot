package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.Hendelse;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HendelseRowMapper implements RowMapper<Hendelse> {

    @Override
    public Hendelse mapRow(ResultSet rs, int rowNum) throws SQLException {
        HendelseType hendelseType = null;
        try {
            String type = rs.getString("hendelse_type");
            if (type != null) {
                hendelseType = HendelseType.valueOf(type);
            }
        } catch (IllegalArgumentException e) {
            hendelseType = HendelseType.OPPRETTET;
        }

        return new Hendelse()
                .medBehandlingsid(rs.getString( "behandlingsid"))
                .medHendelseType(hendelseType)
                .medHendelseTidspunkt(rs.getTimestamp("hendelse_tidspunkt"))
                .medVersjon(rs.getString("versjon"))
                .medSkjemanr(rs.getString("skjemanummer"))
                .medSisteHendelse(rs.getBoolean("sist_hendelse")
                );
    }

}
