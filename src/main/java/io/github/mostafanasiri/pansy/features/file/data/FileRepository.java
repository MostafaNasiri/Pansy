package io.github.mostafanasiri.pansy.features.file.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer> {
    /**
     * Returns file ids that are attached to an entity from a given list of file ids. The result of this method must
     * always be checked before attaching a file to an entity.
     */
    @Query(
            value = """
                    SELECT file_id FROM post_images WHERE file_id IN(?1)
                    UNION
                    SELECT file_id FROM avatar_images WHERE file_id IN(?1)
                    """,
            nativeQuery = true
    )
    List<Integer> getFileIdsThatAreAttachedToAnEntity(List<Integer> fileIds);
}
