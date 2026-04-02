package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<Role, Long> {}