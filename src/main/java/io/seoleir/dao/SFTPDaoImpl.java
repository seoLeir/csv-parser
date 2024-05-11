package io.seoleir.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.seoleir.model.MNPModel;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SFTPDaoImpl implements SFTPDao {

    private final DataSource dataSource;

    @Override
    public void saveNPMModels(List<MNPModel> mnpList) {

        if (!mnpList.isEmpty()) {
            String deleteSQL = """
                DELETE FROM MNP WHERE ID NOT IN (%s);
                """;

            String insertAndUpdateSQL = """
                    INSERT INTO MNP
                    (
                      MSISDN,
                      SMPP_GROUP_ID
                    )
                    VALUES
                        (?, (SELECT smm_g.ID FROM SMPP_GROUPS smm_g WHERE smm_g.mnc = ?))
                    ON DUPLICATE KEY UPDATE SMPP_GROUP_ID = (SELECT smm_g.ID FROM SMPP_GROUPS smm_g WHERE smm_g.mnc = ?)
                    """;

            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);

                String numbers = mnpList.stream()
                        .map(MNPModel::getNumber)
                        .collect(Collectors.joining(", "));

                PreparedStatement pst = conn.prepareStatement(deleteSQL.formatted(numbers));

                int effectedRows = pst.executeUpdate();
                log.info("Successfully deleted {} rows", effectedRows);

                pst = conn.prepareStatement(insertAndUpdateSQL);

                for (MNPModel mnpModel : mnpList) {

                    pst.setString(1, mnpModel.getNumber());
                    pst.setString(2, mnpModel.getRn());
                    pst.setString(3, mnpModel.getRn());

                    pst.executeBatch();
                }

                pst.executeBatch();
                pst.close();
                conn.commit();

                log.info("Successfully inserted: {} rows", mnpList.size());
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
}
