package by.demon.zoom.mapper;

import by.demon.zoom.domain.*;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.dto.lenta.LentaDTO;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.dto.VlookBarDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public final class MappingUtils {

    private static final List<String> listCompetitors = Arrays.asList("auchan.ru", "lenta.com", "metro-cc.ru", "myspar.ru", "okeydostavka.ru", "perekrestok.ru", "winelab.ru");

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

    public static LentaReportDTO mapToLentaReportDTO(Lenta entity) {
        LentaReportDTO lentaReportDTO = new LentaReportDTO();
        lentaReportDTO.setCity(entity.getCity());
        lentaReportDTO.setProduct(entity.getProduct());
        lentaReportDTO.setProductName(entity.getProductName());
        lentaReportDTO.setPrice(entity.getPrice());
        lentaReportDTO.setNetwork(entity.getNetwork());
        lentaReportDTO.setActionPrice1(entity.getActionPrice1());
        lentaReportDTO.setDateFromPromo(entity.getDateFromPromo());
        lentaReportDTO.setDateToPromo(entity.getDateToPromo());
        lentaReportDTO.setDiscountPercentage(entity.getDiscountPercentage());
        lentaReportDTO.setMechanicsOfTheAction(entity.getMechanicsOfTheAction());
        lentaReportDTO.setUrl(entity.getUrl());
        lentaReportDTO.setAdditionalPrice(entity.getAdditionalPrice());
        lentaReportDTO.setModel(entity.getModel());
        lentaReportDTO.setWeightEdeadeal(entity.getWeightEdeadeal());
        lentaReportDTO.setWeightEdeadealKg(entity.getPriceEdeadealKg());
        lentaReportDTO.setWeightLenta(entity.getWeightLenta());
        lentaReportDTO.setWeightLentaKg(entity.getWeightLentaKg());
        lentaReportDTO.setPriceEdeadealKg(entity.getPriceEdeadealKg());
        lentaReportDTO.setConversionToLentaWeight(entity.getConversionToLentaWeight());
        return lentaReportDTO;
    }

    public static SimpleDTO mapToSimpleDTO(Product entity) {
        SimpleDTO simpleDTO = new SimpleDTO();
        simpleDTO.setId(entity.getId());
        simpleDTO.setCategory1(entity.getCategory1());
        simpleDTO.setCategory2(entity.getCategory2());
        simpleDTO.setCategory3(entity.getCategory3());
        simpleDTO.setBrand(entity.getBrand());
        simpleDTO.setModel(entity.getModel());
        simpleDTO.setPrice(entity.getPrice());
        simpleDTO.setCity(entity.getCity());
        simpleDTO.setCompetitor(entity.getCompetitor());
        simpleDTO.setTime(entity.getTime());
        simpleDTO.setDate(entity.getDate());
        simpleDTO.setCompetitorPrice(entity.getCompetitorPrice());
        simpleDTO.setCompetitorOldPrice(entity.getCompetitorOldPrice());
        simpleDTO.setCompetitorActionPrice(entity.getCompetitorActionPrice());
        simpleDTO.setComment(entity.getComment());
        simpleDTO.setCompetitorModel(entity.getCompetitorModel());
        simpleDTO.setCompetitorYear(entity.getYearCompetitor());
        simpleDTO.setAnalogue(entity.getAnalogue());
        simpleDTO.setAddressOfTheCompetitor(entity.getAddressOfTheCompetitor());
        simpleDTO.setStatus(entity.getStatus());
        simpleDTO.setPromo(entity.getPromo());
        simpleDTO.setCompetitorUrl(entity.getCompetitorUrl());
        simpleDTO.setClientUrl(entity.getClientUrl());
        if (!ifExistCompetitor(entity.getCompetitor())){
            simpleDTO.setUrlWebCache(entity.getWebCacheUrl());
        } else {
            simpleDTO.setUrlWebCache("");
        }
        return simpleDTO;
    }

    public static Boolean ifExistCompetitor(String str) {
        return listCompetitors.stream()
                .anyMatch(i -> i.equals(str));
    }
}
