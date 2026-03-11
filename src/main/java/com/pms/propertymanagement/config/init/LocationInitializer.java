package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Province;
import com.pms.propertymanagement.entity.Ward;
import com.pms.propertymanagement.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationInitializer {

    private final ProvinceRepository provinceRepository;

    public void init() {
        if (!provinceRepository.findAll().isEmpty()) return;

        // 1. Thành phố Hồ Chí Minh
        Province hcm = new Province("79", "Thành phố Hồ Chí Minh");
        
        // Quận 1
        hcm.getWards().add(new Ward("26734", "Phường Bến Nghé", hcm));
        hcm.getWards().add(new Ward("26740", "Phường Bến Thành", hcm));
        hcm.getWards().add(new Ward("26743", "Phường Nguyễn Thái Bình", hcm));
        
        // Quận 3
        hcm.getWards().add(new Ward("27121", "Phường Võ Thị Sáu", hcm));
        
        // Quận 7
        hcm.getWards().add(new Ward("27310", "Phường Tân Phong", hcm));
        hcm.getWards().add(new Ward("27313", "Phường Tân Phú", hcm));
        
        // Thành phố Thủ Đức
        hcm.getWards().add(new Ward("26863", "Phường Thảo Điền", hcm));
        hcm.getWards().add(new Ward("26866", "Phường An Phú", hcm));
        hcm.getWards().add(new Ward("27610", "Phường Phước Long", hcm));
        hcm.getWards().add(new Ward("27613", "Tăng Nhơn Phú", hcm));
        hcm.getWards().add(new Ward("26881", "Phường Hiệp Phú", hcm)); // Gần khu Công nghệ cao
        hcm.getWards().add(new Ward("26875", "Phường Long Thạnh Mỹ", hcm)); // Gần Vinhome Grand Park
        hcm.getWards().add(new Ward("26878", "Phường Long Bình", hcm));
        hcm.getWards().add(new Ward("26844", "Phường Linh Xuân", hcm)); // Làng Đại Học ĐHQG TP.HCM

        // Quận Bình Thạnh
        hcm.getWards().add(new Ward("27151", "Phường 25", hcm)); // Gần Hutech, Ngoại thương
        
        provinceRepository.save(hcm);


        // 2. Thành phố Hà Nội
        Province hn = new Province("01", "Thành phố Hà Nội");
        
        // Quận Cầu Giấy
        hn.getWards().add(new Ward("00166", "Phường Dịch Vọng", hn));
        hn.getWards().add(new Ward("00169", "Phường Dịch Vọng Hậu", hn)); // Nhiều văn phòng IT
        hn.getWards().add(new Ward("00175", "Phường Mai Dịch", hn));
        
        // Quận Đống Đa
        hn.getWards().add(new Ward("00202", "Phường Láng Hạ", hn));
        hn.getWards().add(new Ward("00214", "Phường Ô Chợ Dừa", hn));
        
        // Quận Hoàn Kiếm
        hn.getWards().add(new Ward("00007", "Phường Hàng Bạc", hn));
        hn.getWards().add(new Ward("00031", "Phường Tràng Tiền", hn));
        
        // Huyện Hoà Lạc (Khu Công Nghệ Cao) - Actually Thạch Thất district
        hn.getWards().add(new Ward("04786", "Xã Tân Xã", hn)); // Gần FPT University HL
        hn.getWards().add(new Ward("04789", "Xã Thạch Hòa", hn));

        provinceRepository.save(hn);


        // 3. Thành phố Đà Nẵng
        Province dn = new Province("48", "Thành phố Đà Nẵng");
        
        // Quận Hải Châu
        dn.getWards().add(new Ward("20197", "Phường Thạch Thang", dn));
        dn.getWards().add(new Ward("20257", "Phường Hòa Cường Bắc", dn));
        
        // Quận Ngũ Hành Sơn
        dn.getWards().add(new Ward("20308", "Phường Mỹ An", dn)); // Khu phố Tây
        dn.getWards().add(new Ward("20311", "Phường Khuê Mỹ", dn));
        dn.getWards().add(new Ward("20314", "Phường Hòa Hải", dn)); // Gần FPT University DN

        // Quận Liên Chiểu
        dn.getWards().add(new Ward("20194", "Phường Hòa Khánh Bắc", dn)); // Gần Bách Khoa
        
        provinceRepository.save(dn);
        
        
        // 4. Tỉnh Bình Dương
        Province bd = new Province("74", "Tỉnh Bình Dương");
        bd.getWards().add(new Ward("25573", "Phường Đông Hòa", bd)); // Dĩ An (Làng ĐH)
        bd.getWards().add(new Ward("25576", "Phường Bình An", bd));
        provinceRepository.save(bd);
    }
}

