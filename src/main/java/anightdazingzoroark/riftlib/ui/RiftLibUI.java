package anightdazingzoroark.riftlib.ui;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.RiftLibJEI;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibTextField;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;

public abstract class RiftLibUI extends GuiScreen {
    private final ResourceLocation popupBackground = new ResourceLocation(RiftLib.ModID, "textures/ui/popup.png");
    private final Map<String, Double> sectionScrollProgress = new HashMap<>();
    private String sectionWithHeightChange = "";
    protected final String popupID = "popup";
    private List<RiftLibUISection> uiSections = new ArrayList<>();
    private RiftLibUISection popupSection;

    //for managing sections
    private final List<String> hiddenUISections = new ArrayList<>();

    //position
    public final int x;
    public final int y;
    public final int z;

    public RiftLibUI(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    //get all the sections that make up the ui
    public abstract List<RiftLibUISection> uiSections();

    //add the background texture
    public abstract ResourceLocation drawBackground();

    //background texture size is an array, 0 is width and 1 is height
    public abstract int[] backgroundTextureSize();

    //background uv is an array, 0 is x and 1 is y
    public abstract int[] backgroundUV();

    //background size is an array, 0 is width and 1 is height
    public abstract int[] backgroundSize();

    @Override
    public void initGui() {
        super.initGui();

        //set contents of sections when opening screen
        if (this.uiSections.isEmpty()) {
            this.uiSections = this.uiSections();

            //initialize scroll progress for all the sections
            for (RiftLibUISection section : this.uiSections) {
                if (this.sectionScrollProgress.containsKey(section.id)) {
                    this.sectionScrollProgress.put(section.id, 0D);
                }
            }
        }
        //when screen resizes, make sure that the section and all changes to it are preserved
        //only thing that changes is what it believes to be the ui size
        else {
            for (RiftLibUISection section : this.uiSections) {
                section.resizeGUISizes(this.width, this.height);
            }
        }

        //if popup exists, make sure that the section and all changes to it are preserved
        //when resizing the screen
        if (this.popupSection != null) this.popupSection.resizeGUISizes(this.width, this.height);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //background
        this.drawGuiContainerBackgroundLayer();

        //hovered elements will be defined when iterating over the sections
        ItemStack hoveredItem = null;
        RiftLibUISection sectionWithHoveredElement = null;
        RiftLibUIElement.Element hoveredElement = null;
        String overlayText = "";

        //iterate over all ui sections
        for (RiftLibUISection section : this.uiSections) {
            //draw all the sections in uiSections as long as its textBoxID is not hidden
            if (!this.hiddenUISections.contains(section.id)) {
                //define contents
                section.setSectionContents();

                //when there's a popup, make sure that hover related effects cannot happen
                section.setCanDoHoverEffects(this.popupSection == null);

                //put in the method that allows for additional section element modification
                for (int i = 0; i < section.getSectionContents().size(); i++) {
                    RiftLibUIElement.Element element = section.getSectionContents().get(i);
                    element = this.modifyUISectionElement(section, element);
                    section.setSectionContent(i, element);
                }

                //put in the method that allows for additional section modification
                section = this.modifyUISection(section);

                //draw section
                section.drawSectionContents(mouseX, mouseY, partialTicks);

                //if there's a section whose height was changed, update it here
                if (!this.sectionWithHeightChange.isEmpty()) {
                    double oldScrollProgress = this.sectionScrollProgress.get(this.sectionWithHeightChange);
                    int newMaxScroll = section.getMaxScroll();
                    section.setScrollOffset((int) (newMaxScroll * oldScrollProgress));
                    this.sectionWithHeightChange = "";
                }
            }

            //if theres a popup, skip the hoverlay related stuff
            if (this.popupSection != null) continue;

            //assign hovered item as long as its originally null
            if (hoveredItem == null) hoveredItem = section.getHoveredItemStack(mouseX, mouseY);

            //if element has hover effects, do them
            RiftLibUIElement.Element elementToTest = section.getHoveredElement(mouseX, mouseY);
            if (elementToTest != null && !this.hiddenUISections.contains(section.id)) {
                if (elementToTest instanceof RiftLibUIElement.ToolElement) {
                    sectionWithHoveredElement = section;
                    hoveredElement = elementToTest;
                    overlayText = section.getToolHoverText(mouseX, mouseY);
                }
                else if (elementToTest.hasOverlayEffects()) {
                    sectionWithHoveredElement = section;
                    hoveredElement = elementToTest;
                    overlayText = elementToTest.getOverlayText();
                }
            }
        }

        //show overlay info regarding hovered item
        if (hoveredItem != null) {
            List<String> tooltip = new ArrayList<>();

            tooltip.add(hoveredItem.getDisplayName());
            if (Loader.isModLoaded(RiftLibJEI.JEI_MOD_ID)) tooltip.add(I18n.format("ui.open_in_jei"));
            this.drawHoveringText(tooltip, mouseX, mouseY);
        }

        //show hoverlay over a hovered element
        if (sectionWithHoveredElement != null && hoveredElement != null) {
            this.onElementHovered(sectionWithHoveredElement, hoveredElement);
            if (!overlayText.isEmpty()) this.drawHoveringText(overlayText, mouseX, mouseY);
        }

        //create popup and a black gradient over the ui for when a popup has been opened
        if (this.popupSection != null) {
            this.drawVerticalBlackGradientOverlay(
                    (this.width - this.backgroundSize()[0]) / 2,
                    (this.height - this.backgroundSize()[1]) / 2,
                    this.backgroundSize()[0],
                    this.backgroundSize()[1],
                    128
            );
            this.drawPopupBackgroundLayer();

            //define contents
            this.popupSection.setSectionContents();

            //force contents of section to be centered
            this.popupSection.setContentsCenteredVertically(true);

            //draw popup elements
            this.popupSection.drawSectionContents(mouseX, mouseY, partialTicks);
        }
    }

    public abstract RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection section, RiftLibUIElement.Element oldElement);

    public abstract RiftLibUISection modifyUISection(RiftLibUISection oldSection);

    private void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.drawBackground());
        int k = (this.width - this.backgroundSize()[0]) / 2;
        int l = (this.height - this.backgroundSize()[1]) / 2;
        drawModalRectWithCustomSizedTexture(k, l,
                this.backgroundUV()[0],
                this.backgroundUV()[1],
                this.backgroundSize()[0],
                this.backgroundSize()[1],
                this.backgroundTextureSize()[0],
                this.backgroundTextureSize()[1]
        );
    }

    private void drawPopupBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.popupBackground);
        int k = (this.width - 176) / 2;
        int l = (this.height - 96) / 2;
        drawModalRectWithCustomSizedTexture(k, l,
                0,
                0,
                176,
                96,
                176,
                96
        );
    }

    public abstract void onButtonClicked(RiftLibButton button);

    public abstract void onClickableSectionClicked(RiftLibClickableSection clickableSection);

    public abstract void onElementHovered(RiftLibUISection hoveredSection, RiftLibUIElement.Element hoveredElement);

    protected void createPopup(List<RiftLibUIElement.Element> elements) {
        //this.popup = new RiftLibPopupUI(elements, this.width, this.height, this.fontRenderer, this.mc);
        this.popupSection = new RiftLibUISection(this.popupID, this.width, this.height, 166, 86, 0, 0, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                return elements;
            }
        };
    }

    protected RiftLibUISection getPopupSection() {
        return this.popupSection;
    }

    protected void clearPopup() {
        this.popupSection = null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            //scroll within only the popup if there is one
            if (this.popupSection != null) this.popupSection.handleScrollWithScrollbar(mouseX, mouseY, delta);
            //scroll within sections if theres no popup
            else {
                for (RiftLibUISection section : this.uiSections) {
                    //skip if this section is hidden
                    if (this.hiddenUISections.contains(section.id)) continue;

                    //handle scrolling with scrollbar on sections
                    section.handleScrollWithScrollbar(mouseX, mouseY, delta);

                    //update scroll map
                    this.sectionScrollProgress.put(
                            section.id,
                            section.getScrollOffset() / (double) section.getMaxScroll()
                    );
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //if theres a popup, prioritize it
        if (this.popupSection != null) this.mouseClickSection(this.popupSection, mouseX, mouseY, mouseButton);
        else {
            for (RiftLibUISection section : this.uiSections) {
                //skip if this section is hidden
                if (this.hiddenUISections.contains(section.id)) continue;

                //general click section method
                this.mouseClickSection(section, mouseX, mouseY, mouseButton);
            }
        }
    }

    protected void mouseClickSection(RiftLibUISection section, int mouseX, int mouseY, int mouseButton) {
        //anti-nullpointerexception protection
        if (section == null) return;

        //skip to clicked position scroll bar on sections
        section.handleClickOnScrollSection(mouseX, mouseY, mouseButton);

        //update scroll map
        this.sectionScrollProgress.put(
                section.id,
                section.getScrollOffset() / (double) section.getMaxScroll()
        );

        //open jei for items
        section.itemElementClicked(mouseX, mouseY, mouseButton);

        //all the additional logic here is for ensuring clicking smth out of bounds results in nothing
        int sectionTop = (this.height - section.sectionSize()[1]) / 2 + section.sectionPos()[1];
        int sectionBottom = sectionTop + section.sectionSize()[1];

        //button clicking
        for (RiftLibButton button : section.getActiveButtons()) {
            int buttonTop = button.y;
            int buttonBottom = button.y + button.height;
            boolean clickWithinVisiblePart = mouseY >= Math.max(buttonTop, sectionTop) && mouseY <= Math.min(buttonBottom, sectionBottom);
            if (clickWithinVisiblePart && button.mousePressed(this.mc, mouseX, mouseY)) {
                this.playPressSound();
                this.onButtonClicked(button);
            }
        }

        //clickable section clicking
        for (RiftLibClickableSection clickableSection : section.getClickableSections()) {
            int clickableSectionTop = clickableSection.minClickableArea()[1];
            int clickableSectionBottom = clickableSection.maxClickableArea()[1];
            boolean clickWithinVisiblePart = mouseY >= Math.max(clickableSectionTop, sectionTop) && mouseY <= Math.min(clickableSectionBottom, sectionBottom);
            if (clickWithinVisiblePart && clickableSection.isHovered(mouseX, mouseY)) {
                this.playPressSound();
                this.onClickableSectionClicked(clickableSection);
            }
        }

        //tab selector clicking
        for (RiftLibUISection.TabSelectorClickRegion tabSelectorClickRegion : section.getTabSelectorClickRegions()) {
            if (tabSelectorClickRegion.isHovered(mouseX, mouseY)) {
                this.playPressSound();
                section.getOpenedTabs().replace(tabSelectorClickRegion.tabID, tabSelectorClickRegion.tabContentsID);
                //this is for changing scroll related stuff for later
                this.sectionWithHeightChange = section.id;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        //if theres a popup, prioritize it
        if (this.popupSection != null) this.popupSection.handleReleaseClickOnScrollbar(mouseX, mouseY, state);
        else {
            for (RiftLibUISection section : this.uiSections) {
                //skip if this section is hidden
                if (this.hiddenUISections.contains(section.id)) continue;

                section.handleReleaseClickOnScrollbar(mouseX, mouseY, state);

                //update scroll map
                this.sectionScrollProgress.put(
                        section.id,
                        section.getScrollOffset() / (double) section.getMaxScroll()
                );
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        //if theres a popup, prioritize it
        if (this.popupSection != null) this.popupSection.handleScrollWithClick(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        else {
            for (RiftLibUISection section : this.uiSections) {
                //skip if this section is hidden
                if (this.hiddenUISections.contains(section.id)) continue;

                section.handleScrollWithClick(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

                //update scroll map
                this.sectionScrollProgress.put(
                        section.id,
                        section.getScrollOffset() / (double) section.getMaxScroll()
                );
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.popupSection != null) {
            //make sure that pressing escape when theres a popup removes the popup
            if (keyCode == 1) this.clearPopup();

            //manage typing within the popup
            this.keyTypedSection(this.popupSection, typedChar, keyCode);
        }
        else {
            //for exiting gui in general when pressing escape, as well as other functions involving keyboard
            if (keyCode == 1) this.onPressEscape();

            //deal with input for text boxes in each section
            for (RiftLibUISection section : this.uiSections) {
                //skip if this section is hidden
                if (this.hiddenUISections.contains(section.id)) continue;

                this.keyTypedSection(section, typedChar, keyCode);
            }
        }
    }

    protected void keyTypedSection(RiftLibUISection section, char typedChar, int keyCode) {
        //anti-nullpointerexception protection
        if (section == null) return;

        for (RiftLibTextField textField : section.getTextFields()) {
            //get contents
            Map<String, String> contents = section.getTextFieldContents();

            //add to contents map if it exists
            if (contents.containsKey(textField.textBoxID)) {
                textField.textboxKeyTyped(typedChar, keyCode);
                contents.replace(textField.textBoxID, textField.getText());
            }
            //just put it inside otherwise
            else contents.put(textField.textBoxID, String.valueOf(typedChar));
        }
    }

    //effects that take place when pressing escape, can be overridden for custom stuff
    protected void onPressEscape() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) this.mc.setIngameFocus();
    }

    @Override
    public void updateScreen() {
        //if theres a popup, prioritize it
        if (this.popupSection != null) {
            for (RiftLibTextField textField : this.popupSection.getTextFields()) {
                textField.updateCursorCounter();
            }
        }
        else {
            //update text boxes in each section
            for (RiftLibUISection section : this.uiSections) {
                //skip if this section is hidden
                if (this.hiddenUISections.contains(section.id)) continue;

                for (RiftLibTextField textField : section.getTextFields()) {
                    textField.updateCursorCounter();
                }
            }
        }
    }

    //once sections are generated, these methods have to be used to get them
    protected List<RiftLibUISection> getUiSections() {
        return this.uiSections;
    }

    protected RiftLibUISection getSectionByID(String sectionID) {
        for (RiftLibUISection section : this.uiSections) {
            if (section.id.equals(sectionID)) return section;
        }
        return null;
    }

    //button management starts here
    protected boolean buttonInSection(String sectionID, RiftLibButton button) {
        if (this.popupSection != null) {
            //get popup section
            return this.popupSection.getActiveButtons().contains(button);
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                if (section.id.equals(sectionID)) return section.getActiveButtons().contains(button);
            }
        }
        return false;
    }

    protected boolean buttonIsUsable(String sectionID, String buttonID) {
        if (this.popupSection != null) {
            for (RiftLibButton button : this.popupSection.getActiveButtons()) {
                if (button.buttonId.equals(buttonID)) return this.popupSection.buttonIsEnabled(buttonID);
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                if (section.id.equals(sectionID)) {
                    for (RiftLibButton button : section.getActiveButtons()) {
                        if (button.buttonId.equals(buttonID)) return section.buttonIsEnabled(buttonID);
                    }
                }
            }
        }
        return false;
    }

    protected RiftLibButton getButtonByID(String id) {
        if (this.popupSection != null) {
            for (RiftLibButton button : this.popupSection.getActiveButtons()) {
                if (button.buttonId.equals(id)) return button;
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                for (RiftLibButton button : section.getActiveButtons()) {
                    if (button.buttonId.equals(id)) return button;
                }
            }
        }
        return null;
    }

    protected void setButtonUsabilityByID(String id, boolean value) {
        if (this.popupSection != null) {
            for (RiftLibButton button : this.popupSection.getActiveButtons()) {
                if (button.buttonId.equals(id)) this.popupSection.setButtonEnabled(id, value);
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                for (RiftLibButton button : section.getActiveButtons()) {
                    if (button.buttonId.equals(id)) section.setButtonEnabled(id, value);
                }
            }
        }
    }

    protected void setAllButtonsUsability(String sectionID, boolean value) {
        if (this.popupSection != null) {
            for (RiftLibButton button : this.popupSection.getActiveButtons()) {
                this.popupSection.setButtonEnabled(button.buttonId, value);
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                if (section.id.equals(sectionID)) {
                    for (RiftLibButton button : section.getActiveButtons()) {
                        section.setButtonEnabled(button.buttonId, value);
                    }
                }
            }
        }
    }
    //button management ends here

    //clickable section management starts here
    protected boolean clickableSectionInSection(String sectionID, RiftLibClickableSection clickableSection) {
        if (this.popupSection != null) {
            //get popup section
            return this.popupSection.getClickableSections().contains(clickableSection);
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                if (section.id.equals(sectionID)) return section.getClickableSections().contains(clickableSection);
            }
        }
        return false;
    }

    protected RiftLibClickableSection getClickableSectionByID(String id) {
        if (this.popupSection != null) {
            for (RiftLibClickableSection clickableSection : this.popupSection.getClickableSections()) {
                if (clickableSection.getStringID().equals(id)) return clickableSection;
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                for (RiftLibClickableSection clickableSection : section.getClickableSections()) {
                    if (clickableSection.getStringID().equals(id)) return clickableSection;
                }
            }
        }
        return null;
    }

    protected void setSelectClickableSectionByID(String id, boolean value) {
        this.setSelectClickableSectionByID(true, id, value);
    }

    protected void setSelectClickableSectionByID(boolean discriminate, String id, boolean value) {
        if (discriminate) {
            if (this.popupSection != null) {
                for (RiftLibClickableSection clickableSection : this.popupSection.getClickableSections()) {
                    if (clickableSection.getStringID().equals(id)) this.popupSection.setClickableSectionSelected(id, value);
                }
            }
            else {
                for (RiftLibUISection section : this.uiSections) {
                    for (RiftLibClickableSection clickableSection : section.getClickableSections()) {
                        if (clickableSection.getStringID().equals(id)) section.setClickableSectionSelected(id, value);
                    }
                }
            }
        }
        else {
            if (this.popupSection != null) this.popupSection.setClickableSectionSelected(id, value);
            else {
                for (RiftLibUISection section : this.uiSections) {
                    section.setClickableSectionSelected(id, value);
                }
            }
        }
    }
    //clickable section management ends here

    //text box management starts here
    protected String getTextFieldTextByID(String id) {
        if (this.popupSection != null) {
            for (RiftLibTextField textField : this.popupSection.getTextFields()) {
                if (textField.textBoxID.equals(id)) return textField.getText();
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                for (RiftLibTextField textField : section.getTextFields()) {
                    if (textField.textBoxID.equals(id)) return textField.getText();
                }
            }
        }
        return null;
    }

    protected void setTextFieldTextByID(String id, String value) {
        if (this.popupSection != null) {
            for (RiftLibTextField textField : this.popupSection.getTextFields()) {
                if (textField.textBoxID.equals(id)) {
                    this.popupSection.setTextBoxContentsByID(id, value);
                }
            }
        }
        else {
            for (RiftLibUISection section : this.uiSections) {
                for (RiftLibTextField textField : section.getTextFields()) {
                    if (textField.textBoxID.equals(id)) {
                        section.setTextBoxContentsByID(id, value);
                    }
                }
            }
        }
    }
    //text box management starts here

    protected void setUISectionVisibility(String sectionID, boolean value) {
        if (value) this.hiddenUISections.remove(sectionID);
        else if (!this.hiddenUISections.contains(sectionID)) this.hiddenUISections.add(sectionID);
    }

    protected void playPressSound() {
        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void drawVerticalBlackGradientOverlay(int x, int y, int width, int height, int alpha) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Top (more transparent)
        buffer.pos(x, y + height, 0).color(0f, 0f, 0f, alpha / 255f).endVertex();
        buffer.pos(x + width, y + height, 0).color(0f, 0f, 0f, alpha / 255f).endVertex();

        // Bottom (more opaque)
        buffer.pos(x + width, y, 0).color(0f, 0f, 0f, alpha / 255f).endVertex();
        buffer.pos(x, y, 0).color(0f, 0f, 0f, alpha / 255f).endVertex();

        tessellator.draw();

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    //helper method to exit screen quickly
    protected void exitScreen() {
        this.mc.displayGuiScreen(null);
    }
}
