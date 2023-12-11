package by.demon.zoom.mapper;

import by.demon.zoom.domain.Lenta;
import by.demon.zoom.domain.Megatop;
import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.MegatopDTO;
import by.demon.zoom.dto.SimpleDTO;
import by.demon.zoom.dto.lenta.LentaDTO;
import by.demon.zoom.dto.lenta.LentaReportDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public final class MappingUtils {

    private static final List<String> listCompetitors = Arrays.asList("auchan.ru", "lenta.com", "metro-cc.ru", "myspar.ru", "okeydostavka.ru", "perekrestok.ru", "winelab.ru");
    public static final List<String> listUsers = Arrays.asList("zms-cron", "zms-mappings-import", "maudau.com.ua", "detmir.ru-2");

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
        megatopDTO.setPrice(entity.getPrice().isEmpty() ? null : Double.parseDouble(entity.getPrice()));
        megatopDTO.setOldPrice(entity.getOldPrice().isEmpty() ? null : Double.parseDouble(entity.getOldPrice()));
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
        lentaReportDTO.setProduct(entity.getProduct().isEmpty() ? null : Double.parseDouble(entity.getProduct()));
        lentaReportDTO.setProductName(entity.getProductName());
        lentaReportDTO.setPrice(entity.getPrice().isEmpty() ? null : Double.parseDouble(entity.getPrice()));
        lentaReportDTO.setNetwork(entity.getNetwork());
        lentaReportDTO.setActionPrice1(entity.getActionPrice1().isEmpty() ? null : Double.parseDouble(entity.getActionPrice1()));
        lentaReportDTO.setDateFromPromo(entity.getDateFromPromo());
        lentaReportDTO.setDateToPromo(entity.getDateToPromo());
        lentaReportDTO.setDiscountPercentage(entity.getDiscountPercentage().isEmpty() ? null : Double.parseDouble(entity.getDiscountPercentage()));
        lentaReportDTO.setMechanicsOfTheAction(entity.getMechanicsOfTheAction());
        lentaReportDTO.setUrl(entity.getUrl());
        lentaReportDTO.setAdditionalPrice(entity.getAdditionalPrice());
        lentaReportDTO.setModel(entity.getModel());
        lentaReportDTO.setWeightEdeadeal(entity.getWeightEdeadeal());
        lentaReportDTO.setWeightEdeadealKg(entity.getPriceEdeadealKg());
        lentaReportDTO.setWeightLenta(entity.getWeightLenta().isEmpty() ? null : Double.parseDouble(entity.getWeightLenta()));
        lentaReportDTO.setWeightLentaKg(entity.getWeightLentaKg());
        lentaReportDTO.setPriceEdeadealKg(entity.getPriceEdeadealKg().isEmpty() ? null : Double.parseDouble(entity.getPriceEdeadealKg()));
        lentaReportDTO.setConversionToLentaWeight(entity.getConversionToLentaWeight().isEmpty() ? null : Double.parseDouble(entity.getConversionToLentaWeight()));
        lentaReportDTO.setAdditionalField(entity.getAdditionalField());
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
        simpleDTO.setPrice(product.getPrice().isEmpty() ? null : Double.parseDouble(product.getPrice().replace(",", ".")));
        simpleDTO.setCity(product.getCity());
        simpleDTO.setCompetitor(product.getCompetitor());
        simpleDTO.setTime(product.getTime());
        simpleDTO.setDate(product.getDate());
        simpleDTO.setCompetitorPrice(product.getCompetitorPrice().isEmpty() ? null : Double.parseDouble(product.getCompetitorPrice().replace(",", ".")));
        simpleDTO.setCompetitorOldPrice(product.getCompetitorOldPrice().isEmpty() ? null : Double.parseDouble(product.getCompetitorOldPrice().replace(",", ".")));
        simpleDTO.setCompetitorActionPrice(product.getCompetitorActionPrice().isEmpty() ? null : Double.parseDouble(product.getCompetitorActionPrice().replace(",", ".")));
        simpleDTO.setComment(product.getComment());
        simpleDTO.setCompetitorModel(product.getCompetitorModel());
        simpleDTO.setCompetitorYear(product.getYearCompetitor());
        simpleDTO.setAnalogue(product.getAnalogue());
        simpleDTO.setAddressOfTheCompetitor(product.getAddressOfTheCompetitor());
        simpleDTO.setStatus(product.getStatus());
        simpleDTO.setPromo(product.getPromo());
        simpleDTO.setCompetitorUrl(product.getCompetitorUrl());
        simpleDTO.setClientUrl(product.getClientUrl());
        simpleDTO.setUrlWebCache(!ifExistCompetitor(product.getCompetitor(), listCompetitors) ? product.getWebCacheUrl() : "");
        return simpleDTO;
    }

    public static Boolean ifExistCompetitor(String str, List<String> list) {
        return list.stream()
                .anyMatch(i -> i.equals(str));
    }
}
