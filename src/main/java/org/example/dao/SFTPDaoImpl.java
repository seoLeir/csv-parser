package org.example.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.MNPModel;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SFTPDaoImpl implements SFTPDao {

    private final DataSource dataSource;

    @Override
    public void saveNPMModels(List<MNPModel> mnpList) {
        String sql = "INSERT INTO MNP\n" +
                "(\n" +
                " MSISDN,\n" +
                " SMPP_GROUP_ID\n" +
                ")\n" +
                "VALUES\n" +
                "(?, (SELECT smm_g.ID FROM SMPP_GROUPS smm_g WHERE smm_g.mnc = ?))\n" +
                "ON DUPLICATE KEY UPDATE SMPP_GROUP_ID = (SELECT smm_g.ID FROM SMPP_GROUPS smm_g WHERE smm_g.mnc = ?)";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pst = conn.prepareStatement(sql);

            for (MNPModel mnpModel : mnpList) {

                pst.setString(1, mnpModel.getNumber());
                pst.setString(2, mnpModel.getRn());
                pst.setString(3, mnpModel.getRn());

                pst.executeBatch();
            }

            pst.executeBatch();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
    }
}
