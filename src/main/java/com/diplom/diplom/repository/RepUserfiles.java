package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.entity.EntUserfiles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepUserfiles extends CrudRepository<EntUserfiles,Long> {
    Optional<EntUserfiles> findByFilesuserIdAndType(EntUser filesuserId, EntUserfiles.fileType type);
}
