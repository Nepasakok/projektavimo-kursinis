package autodviratis.web;

import autodviratis.model.BikeType;
import autodviratis.model.Listing;
import autodviratis.model.Tenant;
import autodviratis.service.ListingNotFoundException;
import autodviratis.service.ListingService;
import autodviratis.service.NoDataAvailableException;
import autodviratis.service.SourceStatus;
import autodviratis.service.TenantService;
import autodviratis.sources.SwitchableListingSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ListingController {

    private static final String ERROR_VIEW = "error";
    private static final String TENANT_NOT_FOUND = "Klientas nerastas";
    private static final String SOURCE_NOT_FOUND = "Šaltinis nerastas";
    private static final String LISTING_NOT_FOUND =
            "Skelbimas nerastas: jis nepriklauso šiam klientui arba jau paslėptas.";
    private static final String STATUS_UNKNOWN =
            ": būsena dar nežinoma – paspauskite „Atnaujinti skelbimus“.";

    private final ListingService listingService;
    private final TenantService tenantService;
    private final List<SwitchableListingSource> sources;

    public ListingController(ListingService listingService, TenantService tenantService,
                             List<SwitchableListingSource> sources) {
        this.listingService = listingService;
        this.tenantService = tenantService;
        this.sources = sources;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tenants", tenantService.findAll());
        return "index";
    }

    @GetMapping("/t/{tenantId}/listings")
    public String listings(@PathVariable String tenantId, FilterForm filterForm, Model model) {
        Tenant tenant = findTenant(tenantId);
        List<Listing> listings = listingService.search(tenant, filterForm.toFilter(), filterForm.toSortOrder());
        List<ListingRow> rows = new ArrayList<>();
        for (Listing listing : listings) {
            rows.add(ListingRow.from(listing));
        }
        model.addAttribute("tenant", tenant);
        model.addAttribute("rows", rows);
        model.addAttribute("sources", buildSourceViews(tenant));
        model.addAttribute("typeOptions", buildTypeOptions());
        model.addAttribute("warnings", filterForm.invalidFields());
        return "listings";
    }

    @PostMapping("/t/{tenantId}/refresh")
    public String refresh(@PathVariable String tenantId, RedirectAttributes redirectAttributes) {
        Tenant tenant = findTenant(tenantId);
        try {
            listingService.refresh(tenant);
        } catch (NoDataAvailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/t/" + tenantId + "/listings";
    }

    @PostMapping("/t/{tenantId}/listings/{listingId}/hide")
    public String hide(@PathVariable String tenantId, @PathVariable Long listingId) {
        Tenant tenant = findTenant(tenantId);
        listingService.hide(tenant, listingId);
        return "redirect:/t/" + tenantId + "/listings";
    }

    @PostMapping("/t/{tenantId}/sources/{sourceName}/toggle")
    public String toggleSource(@PathVariable String tenantId, @PathVariable String sourceName) {
        Tenant tenant = findTenant(tenantId);
        for (SwitchableListingSource source : sources) {
            if (source.getName().equals(sourceName)) {
                source.setEnabled(tenant, !source.isEnabled(tenant));
                return "redirect:/t/" + tenantId + "/listings";
            }
        }
        throw new WebNotFoundException(SOURCE_NOT_FOUND);
    }

    @ExceptionHandler(WebNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(WebNotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return ERROR_VIEW;
    }

    @ExceptionHandler(ListingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleListingNotFound(ListingNotFoundException e, Model model) {
        model.addAttribute("message", LISTING_NOT_FOUND);
        return ERROR_VIEW;
    }

    private Tenant findTenant(String tenantId) {
        Optional<Tenant> tenant = tenantService.findById(tenantId);
        if (tenant.isEmpty()) {
            throw new WebNotFoundException(TENANT_NOT_FOUND);
        }
        return tenant.get();
    }

    private List<SourceView> buildSourceViews(Tenant tenant) {
        List<SourceStatus> statuses = listingService.getSourceStatuses(tenant);
        List<SourceView> views = new ArrayList<>();
        for (SwitchableListingSource source : sources) {
            boolean enabled = source.isEnabled(tenant);
            Optional<SourceStatus> status = findStatus(statuses, source.getName());
            if (status.isPresent()) {
                boolean unavailable = status.get().state() == SourceStatus.State.UNAVAILABLE;
                views.add(new SourceView(source.getName(), enabled, status.get().message(), unavailable));
            } else {
                views.add(new SourceView(source.getName(), enabled, source.getName() + STATUS_UNKNOWN, false));
            }
        }
        return views;
    }

    private Optional<SourceStatus> findStatus(List<SourceStatus> statuses, String sourceName) {
        for (SourceStatus status : statuses) {
            if (status.sourceName().equals(sourceName)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }

    private List<TypeOption> buildTypeOptions() {
        List<TypeOption> options = new ArrayList<>();
        for (BikeType type : BikeType.values()) {
            if (type != BikeType.NOT_SPECIFIED) {
                options.add(new TypeOption(type.name(), Labels.type(type)));
            }
        }
        return options;
    }
}
