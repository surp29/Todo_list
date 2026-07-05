package com.example.todolist.mapper;

import com.example.todolist.dto.TodoRequestDTO;
import com.example.todolist.dto.TodoResponseDTO;
import com.example.todolist.model.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper converting between {@link Todo} entities and their DTO representations.
 * The {@code assignee}/{@code createdBy} associations are resolved and set by the service layer,
 * not by this mapper, since the request DTO only carries their ids.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TodoMapper {

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeName", source = "assignee.fullName")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    TodoResponseDTO toResponseDTO(Todo todo);

    List<TodoResponseDTO> toResponseDTOList(List<Todo> todos);

    Todo toEntity(TodoRequestDTO requestDTO);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "assignee", ignore = true)
    void updateEntityFromDTO(TodoRequestDTO requestDTO, @MappingTarget Todo todo);
}
