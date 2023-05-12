package by.demon.zoom.mapper;

import by.demon.zoom.domain.*;
import by.demon.zoom.dto.DetmirDTO;
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

    public static List<VlookBarDTO> mapToVlookBarDto(Product product) {
        List<VlookBarDTO> list = new ArrayList<>();
        for (String url : product.getCollectionUrl()) {
            VlookBarDTO dto = new VlookBarDTO();
            dto.setId(product.getId());
            dto.setBar(product.getBar());
            dto.setUrl(url);
            list.add(dto);
        }
        return list;
    }

    public static DetmirDTO mapToDetmirDTO(Product product){
        DetmirDTO detmirStatDTO = new DetmirDTO();
        detmirStatDTO.setClient(product.getClient());
        detmirStatDTO.setIdLink(product.getIdLink());
        detmirStatDTO.setClientId(product.getId());
        detmirStatDTO.setParentCategory(product.getParentCategory());
        detmirStatDTO.setCategory(product.getCategory());
        detmirStatDTO.setVendor(product.getVendor());
        detmirStatDTO.setModel(product.getModel());
        detmirStatDTO.setProductCode(product.getProductCode());
        detmirStatDTO.setBar(product.getBar());
        detmirStatDTO.setStatus(product.getStatus());
        detmirStatDTO.setCompetitorPrice(product.getCompetitorPrice());
        detmirStatDTO.setCompetitorModel(product.getCompetitorModel());
        detmirStatDTO.setCompetitorProductCode(product.getCompetitorProductCode());
        detmirStatDTO.setCompetitorId(product.getCompetitorId());
        detmirStatDTO.setCompetitor(product.getCompetitor());
        detmirStatDTO.setOn(product.getOn());
        return detmirStatDTO;
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

    public static SimpleDTO mapToSimpleDTO(Product product) {
        SimpleDTO simpleDTO = new SimpleDTO();
        simpleDTO.setId(product.getId());
        simpleDTO.setCategory1(product.getCategory1());
        simpleDTO.setCategory2(product.getCategory2());
        simpleDTO.setCategory3(product.getCategory3());
        simpleDTO.setBrand(product.getBrand());
        simpleDTO.setModel(product.getModel());
        simpleDTO.setPrice(product.getPrice());
        simpleDTO.setCity(product.getCity());
        simpleDTO.setCompetitor(product.getCompetitor());
        simpleDTO.setTime(product.getTime());
        simpleDTO.setDate(product.getDate());
        simpleDTO.setCompetitorPrice(product.getCompetitorPrice());
        simpleDTO.setCompetitorOldPrice(product.getCompetitorOldPrice());
        simpleDTO.setCompetitorActionPrice(product.getCompetitorActionPrice());
        simpleDTO.setComment(product.getComment());
        simpleDTO.setCompetitorModel(product.getCompetitorModel());
        simpleDTO.setCompetitorYear(product.getYearCompetitor());
        simpleDTO.setAnalogue(product.getAnalogue());
        simpleDTO.setAddressOfTheCompetitor(product.getAddressOfTheCompetitor());
        simpleDTO.setStatus(product.getStatus());
        simpleDTO.setPromo(product.getPromo());
        simpleDTO.setCompetitorUrl(product.getCompetitorUrl());
        simpleDTO.setClientUrl(product.getClientUrl());
        if (!ifExistCompetitor(product.getCompetitor())){
            simpleDTO.setUrlWebCache(product.getWebCacheUrl());
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
