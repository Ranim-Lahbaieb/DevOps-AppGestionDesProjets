package tn.esprit.backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.entity.Equipe;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.repository.EntrepriseRepository;
import tn.esprit.backend.repository.EquipeRepository;
import tn.esprit.backend.repository.ProjetRepository;
import tn.esprit.backend.service.IEquipeService;

import java.util.List;

@Service
@AllArgsConstructor
public class EquipeServiceImpl implements IEquipeService {

    EquipeRepository equipeRepository;
    EntrepriseRepository entrepriseRepository;
    ProjetRepository projetRepository;

    @Override
    public Equipe addEquipe(Equipe equipe) {
        return equipeRepository.save(equipe);
    }

    @Override
    public Equipe updateEquipe(Equipe equipe) {
        return equipeRepository.save(equipe);
    }

    @Override
    public void deleteEquipe(Long id) {
        equipeRepository.deleteById(id);
    }

    @Override
    public Equipe getEquipeById(Long id) {
        return equipeRepository.findById(id).orElse(null);
    }

    @Override
    public List<Equipe> getAllEquipes() {
        return equipeRepository.findAll();
    }

    @Override
    public List<Equipe> getEquipesByEntreprise(Long entrepriseId) {
        return equipeRepository.findByEntrepriseId(entrepriseId);
    }

    @Override
    public Equipe assignEquipeToEntreprise(Long equipeId, Long entrepriseId) {
        Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + equipeId));
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId).orElseThrow(() -> new EntityNotFoundException("Entreprise introuvable : " + entrepriseId));
        equipe.setEntreprise(entreprise);
        return equipeRepository.save(equipe);
    }

    @Override
    public Equipe assignEquipeToProjet(Long equipeId, Long projetId) {
        Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + equipeId));
        Projet projet = projetRepository.findById(projetId).orElseThrow(() -> new EntityNotFoundException("Projet introuvable : " + projetId));
        equipe.getProjets().add(projet);
        return equipeRepository.save(equipe);
    }
}
