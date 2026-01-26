package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.Contract;
import com.pms.propertymanagement.repository.ContractRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.ContractService;
import com.pms.propertymanagement.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private final RoomService roomService;
    private final UserRepository userRepository;

    @GetMapping
    public String index() {
        return "redirect:/contracts/list";
    }

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long roomId, Model model) {
        List<Contract> contracts;
        if (roomId != null) {
            contracts = contractRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        } else {
            contracts = contractRepository.findAll();
        }
        model.addAttribute("contracts", contracts);
        model.addAttribute("roomId", roomId);
        return "contracts/list";
    }

    @GetMapping("/create/{roomId}")
    public String createFormForRoom(@PathVariable Long roomId, Model model) {
        LocalDate start = LocalDate.now();
        int termMonth = 6;

        Contract contract = new Contract();
        contract.setMonthlyRent(roomService.getById(roomId).getPrice());
        contract.setDepositAmount(0.0);
        contract.setContractTermMonth(termMonth);
        contract.setPaymentDate(28);
        contract.setPaymentMethod("CASH");
        contract.setStartDate(start);
        contract.setEndDate(start.plusMonths(termMonth));

        model.addAttribute("contract", contract);
        model.addAttribute("roomId", roomId);
        return "contracts/form";
    }

    @PostMapping
    public String create(@ModelAttribute Contract contract,
                         @RequestParam Long roomId,
                         @RequestParam Long tenantId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (contract.getStartDate() == null || contract.getEndDate() == null) {
            model.addAttribute("error", "Vui lòng nhập ngày bắt đầu và ngày kết thúc");
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }
        if (contract.getMonthlyRent() == null) {
            model.addAttribute("error", "Vui lòng nhập tiền thuê");
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }
        if (contract.getDepositAmount() == null) {
            model.addAttribute("error", "Vui lòng nhập tiền cọc");
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }
        if (contract.getContractTermMonth() != null
                && (contract.getContractTermMonth() < 6 || contract.getContractTermMonth() > 12)) {
            model.addAttribute("error", "Kỳ hạn phải từ 6 đến 12 tháng");
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }
        if (contract.getPaymentDate() != null
                && (contract.getPaymentDate() < 28 || contract.getPaymentDate() > 31)) {
            model.addAttribute("error", "Ngày thanh toán phải từ 28 đến 31");
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }
        if (contract.getPaymentMethod() != null
                && !contract.getPaymentMethod().equals("CASH")
                && !contract.getPaymentMethod().equals("BANK_TRANSFER")) {
            model.addAttribute("error", "Phương thức thanh toán không hợp lệ");
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }

        try {
            contract.setRoom(roomService.getById(roomId));
            contract.setTenant(userRepository.findById(tenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant không tồn tại")));
            contractService.createContract(contract);
            redirectAttributes.addFlashAttribute("success", "Đã tạo hợp đồng thành công!");
            return "redirect:/contracts/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roomId", roomId);
            model.addAttribute("contract", contract);
            return "contracts/form";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Contract c = contractService.getById(id);
        model.addAttribute("contract", c);
        return "contracts/view";
    }

    @GetMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) Long userId,
                          RedirectAttributes redirectAttributes) {
        contractService.approveContract(id, userId);
        redirectAttributes.addFlashAttribute("success", "Đã phê duyệt hợp đồng!");
        return "redirect:/contracts/" + id;
    }

    @GetMapping("/{id}/activate")
    public String activate(@PathVariable Long id,
                           @RequestParam(required = false) Long userId,
                           RedirectAttributes redirectAttributes) {
        contractService.activateContract(id, userId);
        redirectAttributes.addFlashAttribute("success", "Đã kích hoạt hợp đồng!");
        return "redirect:/contracts/" + id;
    }
}
