package by.bsuir.bookplatform.mapper;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;

public class DTOMapper {
    private static ModelMapper modelMapper;

    public static synchronized ModelMapper getInstance() {
        if (modelMapper == null) {
            modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        }
        return modelMapper;
    }
}
