package io.seoleir.dao;

import io.seoleir.model.MNPModel;

import java.util.List;

public interface SFTPDao {
    void saveNPMModels(List<MNPModel> mnpList);
}
