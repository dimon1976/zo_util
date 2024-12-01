package by.demon.zoom.mapper;

import by.demon.zoom.domain.Product;
import by.demon.zoom.dto.imp.SimpleDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public final class MappingUtils {

    private static final List<String> listCompetitors = Arrays.asList("auchan.ru", "lenta.com", "metro-cc.ru", "myspar.ru", "okeydostavka.ru", "perekrestok.ru", "winelab.ru");
    public static final List<String> listUsers = Arrays.asList("zms-cron", "zms-mappings-import", "maudau.com.ua", "detmir.ru-2");


    public static SimpleDTO mapToSimpleDTO(Product product) {
        SimpleDTO simpleDTO = new SimpleDTO();
        simpleDTO.setId(product.getId());
        simpleDTO.setCategory1(product.getCategory1());
        simpleDTO.setCategory2(product.getCategory2());
        simpleDTO.setCategory3(product.getCategory3());
        simpleDTO.setBrand(product.getBrand());
        simpleDTO.setModel(product.getModel());
        simpleDTO.setPrice(product.getPrice().isEmpty() ? 0.0 : Double.parseDouble(product.getPrice().replace(",", ".")));
        simpleDTO.setCity(product.getCity());
        simpleDTO.setCompetitor(product.getCompetitor());
        simpleDTO.setTime(product.getTime());
        simpleDTO.setDate(product.getDate());
        simpleDTO.setCompetitorPrice(product.getCompetitorPrice().isEmpty() ? 0.0 : Double.parseDouble(product.getCompetitorPrice().replace(",", ".")));
        simpleDTO.setCompetitorOldPrice(product.getCompetitorOldPrice().isEmpty() ? 0.0 : Double.parseDouble(product.getCompetitorOldPrice().replace(",", ".")));
        simpleDTO.setCompetitorActionPrice(product.getCompetitorActionPrice().isEmpty() ? 0.0 : Double.parseDouble(product.getCompetitorActionPrice().replace(",", ".")));
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
