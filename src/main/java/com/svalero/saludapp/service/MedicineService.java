package com.svalero.saludapp.service;

import com.svalero.saludapp.domain.Medicine;
import com.svalero.saludapp.domain.dto.MedicineInDto;
import com.svalero.saludapp.domain.dto.MedicineOutDto;
import com.svalero.saludapp.domain.dto.MedicineRegistrationDto;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.repository.MedicineRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<MedicineOutDto> getAll(String name, String manufacturer) {
        List<Medicine> medicines;
        if ((name == null || name.isEmpty()) && (manufacturer == null || manufacturer.isEmpty())) {
            medicines = medicineRepository.findAll();
        } else if (name == null || name.isEmpty()) {
            medicines = medicineRepository.findByManufacturer(manufacturer);
        } else if (manufacturer == null || manufacturer.isEmpty()) {
            medicines = medicineRepository.findByName(name);
        } else {
            medicines = medicineRepository.findByNameAndManufacturer(name, manufacturer);
        }
        return modelMapper.map(medicines, new TypeToken<List<MedicineOutDto>>() {}.getType());
    }

    public Medicine get(long id) throws MedicineNotFoundException {
        return medicineRepository.findById(id)
                .orElseThrow(MedicineNotFoundException::new);
    }

    public MedicineOutDto add(MedicineRegistrationDto medicineInDto) {
        Medicine medicine = modelMapper.map(medicineInDto, Medicine.class);
        Medicine newMedicine = medicineRepository.save(medicine);
        return modelMapper.map(newMedicine, MedicineOutDto.class);
    }

    public MedicineOutDto modify(long medicineId, MedicineInDto medicineInDto) throws MedicineNotFoundException {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(MedicineNotFoundException::new);

        modelMapper.map(medicineInDto, medicine);
        medicineRepository.save(medicine);

        return modelMapper.map(medicine, MedicineOutDto.class);
    }

    public void remove(long id) throws MedicineNotFoundException {
        medicineRepository.findById(id)
                .orElseThrow(MedicineNotFoundException::new);
        medicineRepository.deleteById(id);
    }
}
