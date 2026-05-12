// PageRequest.java
package sim.forum.dto;

import lombok.Data;

@Data

public class PageRequestDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}