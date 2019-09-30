package io.archilab.prox.searchservice.core;

import java.util.UUID;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AbstractEntity {

  @Id
  private UUID id;

  protected AbstractEntity(UUID id) {
	  this.id=id;
  }
}
