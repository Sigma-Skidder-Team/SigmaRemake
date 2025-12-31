package io.github.sst.remake.gui.screen;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.alt.Account;
import io.github.sst.remake.alt.AccountCompareType;
import io.github.sst.remake.alt.AccountSorter;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.*;
import io.github.sst.remake.gui.element.impl.alert.AlertComponent;
import io.github.sst.remake.gui.element.impl.alert.ComponentType;
import io.github.sst.remake.gui.element.impl.alts.AccountElement;
import io.github.sst.remake.gui.element.impl.alts.AccountUI;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.http.MicrosoftUtils;
import io.github.sst.remake.util.io.audio.SoundUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import io.github.sst.remake.gui.element.impl.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AltManagerScreen extends Screen implements IMinecraft {
    private int field21005;
    private float field21006;
    private boolean field21008 = true;
    private ScrollableContentPanel alts;
    private final ScrollableContentPanel altView;
    private Alert loginDialog;
    private Alert deleteAlert;
    private final float field21014 = 0.65F;
    private final float field21015 = 1.0F - this.field21014;
    private final int titleOffset = 30;
    private final AccountElement accountElement;
    private AccountCompareType accountSortType = AccountCompareType.ADDED;
    private String accountFilter = "";
    private final TextField searchBox;

    public static AltManagerScreen instance;

    public AltManagerScreen() {
        super("Alt Manager");
        instance = this;
        this.setListening(false);
        List<String> sortingOptions = new ArrayList<>();
        sortingOptions.add("Alphabetical");
        sortingOptions.add("Bans");
        sortingOptions.add("Date Added");
        sortingOptions.add("Last Used");
        sortingOptions.add("Use count");
        List<String> servers = new ArrayList<>();
        ServerList serverList = new ServerList(client);
        serverList.loadFile();
        int serverListSize = serverList.size();

        for (int i = 0; i < serverListSize; i++) {
            ServerInfo server = serverList.get(i);
            if (!servers.contains(server.address)) {
                servers.add(server.address);
            }
        }

        this.getLoginDialog();
        this.deleteAltAlert();
        this.addToList(
                this.alts = new ScrollableContentPanel(
                        this,
                        "alts",
                        0,
                        114,
                        (int) ((float) client.getWindow().getWidth() * this.field21014) - 4,
                        client.getWindow().getHeight() - 119 - this.titleOffset
                )
        );
        this.addToList(
                this.altView = new ScrollableContentPanel(
                        this,
                        "altView",
                        (int) ((float) client.getWindow().getWidth() * this.field21014),
                        114,
                        (int) ((float) client.getWindow().getWidth() * this.field21015) - this.titleOffset,
                        client.getWindow().getHeight() - 119 - this.titleOffset
                )
        );
        this.alts.setListening(false);
        this.altView.setListening(false);
        this.alts.method13515(false);
        this.altView
                .addToList(
                        this.accountElement = new AccountElement(
                                this.altView,
                                "info",
                                (int) (
                                        (float) client.getWindow().getWidth() * this.field21015
                                                - (float) ((int) ((float) client.getWindow().getWidth() * this.field21015))
                                )
                                        / 2
                                        - 10,
                                this.method13374(),
                                (int) ((float) client.getWindow().getWidth() * this.field21015),
                                500
                        )
                );
        Dropdown filterDropdown = new Dropdown(this, "drop", (int) ((float) client.getWindow().getWidth() * this.field21014) - 220, 44, 200, 32, sortingOptions, 0);
        filterDropdown.method13643(servers, 1);
        filterDropdown.method13656(2);
        this.addToList(filterDropdown);
        filterDropdown.onPress(var2 -> {
            switch (filterDropdown.getIndex()) {
                case 0:
                    this.accountSortType = AccountCompareType.ALPHABETICAL;
                    break;
                case 1:
                    this.accountSortType = AccountCompareType.BANS;
                    List<String> banList = filterDropdown.method13645(1).method13636();
                    int index = filterDropdown.method13645(1).method13640();

                    if (!banList.isEmpty() && index < banList.size()) {
                        this.accountFilter = banList.get(index);
                    } else {
                        this.accountFilter = "";
                    }
                    break;
                case 2:
                    this.accountSortType = AccountCompareType.ADDED;
                    break;
                case 3:
                    this.accountSortType = AccountCompareType.LAST_USED;
                    break;
                case 4:
                    this.accountSortType = AccountCompareType.USE_COUNT;
            }

            this.updateAccountList(false);
        });
        this.addToList(
                this.searchBox = new TextField(
                        this,
                        "textbox",
                        (int) ((float) client.getWindow().getWidth() * this.field21014),
                        44,
                        150,
                        32,
                        TextField.field20741,
                        "",
                        "Search...",
                        FontUtils.HELVETICA_LIGHT_18
                )
        );
        this.searchBox.setFont(FontUtils.HELVETICA_LIGHT_18);
        this.searchBox.addChangeListener(var1 -> this.updateAccountList(false));
        TextButton addButton;
        this.addToList(addButton = new TextButton(this, "btnt", this.getWidth() - 90, 43, 70, 30, ColorHelper.DEFAULT_COLOR, "Add +", FontUtils.HELVETICA_LIGHT_25));
        this.alts.method13242();
        addButton.onClick((var1, var2) -> {
            if (this.method13369()) {
                this.loginDialog.method13603(!this.loginDialog.isHovered());
            }
        });
    }

    private void method13360(Account acc, boolean var2) {
        AccountUI accountUI;
        this.alts.addToList(
                accountUI = new AccountUI(
                        this.alts,
                        acc.name,
                        this.titleOffset,
                        (100 + this.titleOffset / 2) * this.method13370(),
                        this.alts.getWidth() - this.titleOffset * 2 + 4,
                        100,
                        acc
                )
        );
        if (!var2) {
            accountUI.field20805 = new AnimationUtils(0, 0);
        }

        if (Client.INSTANCE.accountManager.currentAccount == acc) {
            accountUI.setAccountListRefreshing(true);
        }

        accountUI.addSomething((var2x, var3) -> {
            if (var3 != 0) {
                this.deleteAlert.onPress(element -> {
                    Client.INSTANCE.accountManager.remove(accountUI.selectedAccount);
                    this.accountElement.handleSelectedAccount(null);
                    this.updateAccountList(false);
                });
                this.deleteAlert.setFocused(true);
                this.deleteAlert.method13603(true);
            } else {
                this.loginToAccount(accountUI);

                this.accountElement.handleSelectedAccount(accountUI.selectedAccount);

                for (CustomGuiScreen var7 : this.alts.getChildren()) {
                    if (!(var7 instanceof VerticalScrollBar)) {
                        for (CustomGuiScreen var9 : var7.getChildren()) {
                            ((AccountUI) var9).method13166(false);
                        }
                    }
                }

                accountUI.method13166(true);
            }
        });

        if (Client.INSTANCE.accountManager.currentAccount == acc) {
            this.accountElement.handleSelectedAccount(accountUI.selectedAccount);
            accountUI.method13167(true, true);
        }
    }

    public void loginToAccount(AccountUI account) {
        account.setLoadingIndicator(true);

        new Thread(() -> {
            if (!Client.INSTANCE.accountManager.login(account.selectedAccount)) {
                account.setErrorState(114);
                SoundUtils.play("error");
                account.setLoadingIndicator(false);
                return;
            }

            this.method13368();
            account.setAccountListRefreshing(true);
            SoundUtils.play("connect");
            this.updateAccountList(false);
            account.setLoadingIndicator(false);
        }).start();
    }

    private void getLoginDialog() {
        AlertComponent header = new AlertComponent(ComponentType.HEADER, "Add Alt", 50);
        AlertComponent firstline1 = new AlertComponent(ComponentType.FIRST_LINE, "Login with your minecraft", 15);
        AlertComponent firstline2 = new AlertComponent(ComponentType.FIRST_LINE, "account here!", 25);
        AlertComponent usernameInput = new AlertComponent(ComponentType.SECOND_LINE, "Username", 50);
        AlertComponent button = new AlertComponent(ComponentType.BUTTON, "Cracked login", 50);
        AlertComponent button2 = new AlertComponent(ComponentType.BUTTON, "Cookie login", 50);
        AlertComponent button3 = new AlertComponent(ComponentType.BUTTON, "Web login", 50);
        this.addToList(this.loginDialog = new Alert(this, "Add alt dialog", true, "Add Alt", header, firstline1, firstline2, usernameInput, button, button2, button3));

        this.loginDialog.onPress(element -> {
            Button clickedButton = this.loginDialog.getClickedButton();
            if (clickedButton != null) {
                switch (clickedButton.getText()) {
                    case "Cracked login": {
                        String username = this.loginDialog.getInputMap().get("Username");
                        if (username != null && !username.isEmpty()) {
                            Account account = new Account(username, "0", Account.STEVE_UUID);
                            if (!Client.INSTANCE.accountManager.has(account)) {
                                Client.INSTANCE.accountManager.add(account);
                            }
                        }
                        this.updateAccountList(false);
                        break;
                    }
                    case "Cookie login": {
                        System.out.println("Cookie login clicked");
                        break;
                    }
                    case "Web login": {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        MicrosoftUtils.acquireMSAuthCode(executor)
                                .thenComposeAsync(msAuthCode -> MicrosoftUtils.acquireMSAccessToken(msAuthCode, executor), executor)
                                .thenComposeAsync(msAccessToken -> MicrosoftUtils.acquireXboxAccessToken(msAccessToken, executor), executor)
                                .thenComposeAsync(xboxAccessToken -> MicrosoftUtils.acquireXboxXstsToken(xboxAccessToken, executor), executor)
                                .thenComposeAsync(xboxXstsData -> MicrosoftUtils.acquireMCAccessToken(xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor), executor)
                                .thenComposeAsync(mcToken -> MicrosoftUtils.login(mcToken, executor), executor)
                                .thenAccept(session -> {
                                    Account account = new Account(session.getUsername(), session.getAccessToken(), session.getUuid());
                                    if (!Client.INSTANCE.accountManager.has(account)) {
                                        Client.INSTANCE.accountManager.add(account);
                                    }

                                    this.updateAccountList(false);
                                })
                                .exceptionally(error -> {
                                    Client.LOGGER.error("Failed to login", error);
                                    SoundUtils.play("error");
                                    this.updateAccountList(false);
                                    return null;
                                });
                        break;
                    }
                }
            }
        });
    }

    private void deleteAltAlert() {
        AlertComponent title = new AlertComponent(ComponentType.HEADER, "Delete?", 50);
        AlertComponent firstLine = new AlertComponent(ComponentType.FIRST_LINE, "Are you sure you want", 15);
        AlertComponent secondLine = new AlertComponent(ComponentType.FIRST_LINE, "to delete this alt?", 40);
        AlertComponent button = new AlertComponent(ComponentType.BUTTON, "Delete", 50);
        this.addToList(this.deleteAlert = new Alert(this, "delete", true, "Delete", title, firstLine, secondLine, button));
    }

    @Override
    public void draw(float partialTicks) {
        this.drawBackground();
        RenderUtils.drawFloatingPanel(
                (int) ((float) client.getWindow().getWidth() * this.field21014),
                114,
                (int) ((float) client.getWindow().getWidth() * this.field21015) - this.titleOffset,
                client.getWindow().getHeight() - 119 - this.titleOffset,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        this.method13367();
        this.drawTitle();
        super.draw(partialTicks);
    }

    private void drawTitle() {
        int xPos = this.x + this.titleOffset;
        int yPos = this.y + this.titleOffset;
        int color = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_40, (float) xPos, (float) yPos, "Jello", color);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_25, (float) (xPos + 87), (float) (yPos + 15), "Alt Manager", color);
    }

    private void method13367() {
        float var3 = 1.0F;

        for (CustomGuiScreen var5 : this.alts.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    if (var7 instanceof AccountUI) {
                        AccountUI accountUI = (AccountUI) var7;
                        if (var7.getY() <= client.getWindow().getHeight() && this.alts.getScrollOffset() == 0) {
                            if (var3 > 0.2F) {
                                accountUI.field20805.changeDirection(AnimationUtils.Direction.FORWARDS);
                            }

                            float var9 = VecUtils.interpolate(accountUI.field20805.calcPercent(), 0.51, 0.82, 0.0, 0.99);
                            accountUI.method13284((int) (-((1.0F - var9) * (float) (var7.getWidth() + 30))));
                            var3 = accountUI.field20805.calcPercent();
                        } else {
                            accountUI.method13284(0);
                            accountUI.field20805.changeDirection(AnimationUtils.Direction.FORWARDS);
                        }
                    }
                }
            }
        }
    }

    private void method13368() {
        for (CustomGuiScreen screen : this.alts.getChildren()) {
            if (!(screen instanceof VerticalScrollBar)) {
                for (CustomGuiScreen child : screen.getChildren()) {
                    AccountUI accountUI = (AccountUI) child;
                    accountUI.setAccountListRefreshing(false);
                }
            }
        }
    }

    private boolean method13369() {
        for (CustomGuiScreen var5 : this.alts.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    if (var7.method13280() != 0 && var7.getX() > this.width) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int method13370() {
        int var3 = 0;

        for (CustomGuiScreen var5 : this.alts.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen ignored : var5.getChildren()) {
                    var3++;
                }
            }
        }

        return var3;
    }

    private void drawBackground() {
        int var3 = this.getMouseX() * -1;
        float var4 = (float) this.getMouseY() / (float) this.getWidth() * -114.0F;
        if (this.field21008) {
            this.field21006 = (float) ((int) var4);
            this.field21005 = var3;
            this.field21008 = false;
        }

        float var5 = var4 - this.field21006;
        float var6 = (float) (var3 - this.field21005);
        RenderUtils.drawImage((float) this.field21005, this.field21006, (float) (this.getWidth() * 2), (float) (this.getHeight() + 114), Resources.panoramaPNG);
        float var7 = 0.5F;
        if (var4 != this.field21006) {
            this.field21006 += var5 * var7;
        }

        if (var3 != this.field21005) {
            this.field21005 = (int) ((float) this.field21005 + var6 * var7);
        }

        RenderUtils.drawRoundedRect(0.0F, 0.0F, (float) this.getWidth(), (float) this.getHeight(), ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.95F));
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            client.openScreen(new TitleScreen());
        }
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        Client.INSTANCE.accountManager.shutdown();
        return config;
    }

    @Override
    public void loadConfig(JsonObject config) {
        for (CustomGuiScreen var5 : this.alts.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    this.alts.method13234(var7);
                }
            }
        }

        this.updateAccountList(true);
    }

    public void updateAccountList(boolean forceRefresh) {
        List<Account> accounts = AccountSorter.sortByInputAltAccounts(this.accountSortType, this.accountFilter, this.searchBox.getText());
        this.addRunnable(() -> {

            int var3 = 0;
            if (alts != null) {
                var3 = alts.getScrollOffset();
                this.removeChildren(alts);
            }

            CustomGuiScreen var4 = this.getChildByName("alts");
            if (var4 != null) {
                this.removeChildren(var4);
            }

            this.showAlert(this.alts = new ScrollableContentPanel(
                    this,
                    "alts",
                    0,
                    114,
                    (int) ((float) client.getWindow().getWidth() * this.field21014) - 4,
                    client.getWindow().getHeight() - 119 - this.titleOffset
            ));

            for (Account var6 : accounts) {
                this.method13360(var6, forceRefresh);
            }

            this.alts.setScrollOffset(var3);
            this.alts.setListening(false);
            this.alts.method13515(false);
        });
    }

    public int method13374() {
        return client.getWindow().getHeight() / 12 + 280 + client.getWindow().getHeight() / 12;
    }
}