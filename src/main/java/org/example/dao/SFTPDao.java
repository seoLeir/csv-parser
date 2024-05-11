package org.example.dao;

import org.example.model.MNPModel;

import java.util.List;

public interface SFTPDao {
    void saveNPMModels(List<MNPModel> mnpList);
}
