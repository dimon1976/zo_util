package by.demon.zoom.mapper;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.domain.VlookBar;
import by.demon.zoom.dto.LentaDTO;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.dto.VlookBarDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public final class MappingUtils {


    public static List<VlookBarDTO> mapToVlookBarDto(VlookBar entity) {
        List<VlookBarDTO> list = new ArrayList<>();
        for (String url : entity.getUrl()) {
            VlookBarDTO dto = new VlookBarDTO();
            dto.setId(entity.getId());
            dto.setBar(entity.getBar());
            dto.setUrl(url);
            list.add(dto);
        }
        return list;
    }

    public static MegatopDTO mapToMegatopDTO(Megatop entity) {
        MegatopDTO megatopDTO = new MegatopDTO();
        megatopDTO.setCategory1(entity.getCategory1());
        megatopDTO.setCategory(entity.getCategory());
        megatopDTO.setHeelHeight(entity.getHeelHeight());
        megatopDTO.setCollection(entity.getCollection());
        megatopDTO.setUpperConstruction(entity.getUpperConstruction());
        megatopDTO.setUpperMaterial(entity.getUpperMaterial());
        megatopDTO.setLiningMaterial(entity.getLiningMaterial());
        megatopDTO.setRostovChildren(entity.getRostovChildren());
        megatopDTO.setColors(entity.getColors());
        megatopDTO.setSeason(entity.getSeason());
        megatopDTO.setCompetitor(entity.getCompetitor());
        megatopDTO.setId(entity.getMegatopId());
        megatopDTO.setCategory2(entity.getCategory2());
        megatopDTO.setBrand(entity.getBrand());
        megatopDTO.setModel(entity.getModel());
        megatopDTO.setVendorCode(entity.getVendorCode());
        megatopDTO.setPrice(entity.getPrice());
        megatopDTO.setOldPrice(entity.getOldPrice());
        megatopDTO.setUrl(entity.getUrl());
        megatopDTO.setStatus(entity.getStatus());
        return megatopDTO;
    }

    public static LentaDTO mapToLentaDTO(Lenta entity) {
        LentaDTO lentaDTO = new LentaDTO();
        String joinEan = String.join(",", entity.getEan());
        lentaDTO.setId(entity.getId());
        lentaDTO.setModel(entity.getModel());
        lentaDTO.setWeight(entity.getWeight());
        lentaDTO.setPrice(entity.getPrice());
        lentaDTO.setMoscow(entity.getMoscow());
        lentaDTO.setRostovNaDonu(entity.getRostovNaDonu());
        lentaDTO.setSpb(entity.getSpb());
        lentaDTO.setNovosibirsk(entity.getNovosibirsk());
        lentaDTO.setYekaterinburg(entity.getYekaterinburg());
        lentaDTO.setSaratov(entity.getSaratov());
        lentaDTO.setEan(joinEan);
        return lentaDTO;
    }
}
