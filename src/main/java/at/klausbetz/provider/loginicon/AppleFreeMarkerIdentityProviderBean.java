package at.klausbetz.provider.loginicon;

import at.klausbetz.provider.AppleIdentityProviderFactory;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.models.OrderedModel;
import org.keycloak.models.RealmModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// copy of org.keycloak.forms.login.freemarker.model.IdentityProviderBean
public class AppleFreeMarkerIdentityProviderBean {

    private static final OrderedModel.OrderedModelComparator<IdentityProviderBean.IdentityProvider> IDP_COMPARATOR_INSTANCE = new OrderedModel.OrderedModelComparator<>();

    private boolean displaySocial;
    private List<IdentityProviderBean.IdentityProvider> providers;

    private RealmModel realm;

    public AppleFreeMarkerIdentityProviderBean(IdentityProviderBean identityProviderBean, RealmModel realm) {
        this.realm = realm;

        if (identityProviderBean.getProviders() == null) {
            return;
        }

        var idps = new ArrayList<>(identityProviderBean.getProviders());
        var appleIdp = idps.stream().filter(idp -> idp.getProviderId().equals(AppleIdentityProviderFactory.PROVIDER_ID)).findFirst();
        IdentityProviderBean.IdentityProvider appleIdpWithIconClass = null;

        if (appleIdp.isPresent()) {
            appleIdpWithIconClass = new IdentityProviderBean.IdentityProvider(appleIdp.get().getAlias(), appleIdp.get().getDisplayName(), appleIdp.get().getProviderId(), appleIdp.get().getLoginUrl(), appleIdp.get().getGuiOrder(), (appleIdp.get().getIconClasses() == null || appleIdp.get().getIconClasses().isBlank()) ? "fa fa-apple" : appleIdp.get().getIconClasses());
        }

        var orderedProviderList = idps.stream().filter(i -> !i.getProviderId().equals(AppleIdentityProviderFactory.PROVIDER_ID)).collect(Collectors.toCollection(ArrayList::new));
        if (appleIdpWithIconClass != null) {
            orderedProviderList.add(appleIdpWithIconClass);
        }

        if (!orderedProviderList.isEmpty()) {
            orderedProviderList.sort(IDP_COMPARATOR_INSTANCE);
            providers = orderedProviderList;
            displaySocial = true;
        }
    }

    public List<IdentityProviderBean.IdentityProvider> getProviders() {
        return providers;
    }

    public boolean isDisplayInfo() {
        return realm.isRegistrationAllowed() || displaySocial;
    }
}
