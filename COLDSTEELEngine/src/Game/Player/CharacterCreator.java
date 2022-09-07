package Game.Player;

import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.capitalize;
import static CSUtil.BigMixin.put;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_MINUS;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_PLUS;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_button_symbol;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_image;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;

import CSUtil.CSTFParser;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.ECS;
import Core.NKUI;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityRPGStats;
import Renderer.Textures.ImageInfo;

/**
 * 
 * Instances of this are created when the player is creating a new character to play. 
 * 
 * @author Chris Brown
 *
 */
public class CharacterCreator implements NKUI {
		
	private static record CharacterCreatorData(String name , NkImage graphic , ImageInfo graphicInfo, String details , Entities entity) {}
	private final CSLinked<CharacterCreatorData> charactersToChooseFrom = new CSLinked<>();
	
	NkRect characterChooserRect = NkRect.malloc(allocator).set(20 , 20 , 730 , 1040);
	NkRect characterStatsRect = NkRect.malloc(allocator).set(785 , 20 , 350 , 1040);
	NkRect characterDetailsRect = NkRect.malloc(allocator).set(1170 , 20 , 730 , 1040);

	String startingLevel;
	String startingDoor;
	
	private ByteBuffer[] checks;

	int availablePoints;
	
	private PlayerCharacter newPlayer = null;	
	private CharacterCreatorData selectedCharacter = null;
	private EntityRPGStats defaultStats = null;
	
	{

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "engine/Character Creator Config.CStf"))){
			
			CSTFParser cstf = new CSTFParser(reader);
			cstf.rname();
			startingLevel = cstf.rlabel("starting level");
			startingDoor = cstf.rlabel("starting door");
			availablePoints = cstf.rintLabel("stat points");
			int numberCharacters = cstf.rlist("entities");
			
			for(int i = 0 ; i < numberCharacters ; i ++) {
			
				String entityName = cstf.rlist();				
				String image = cstf.rlabel("image");
				String details = cstf.rlabel("details");
				
				NkImage imageStruct = NkImage.malloc(allocator);
				ImageInfo info = NKUI.image(CS.COLDSTEEL.assets + image , imageStruct);
				charactersToChooseFrom.add(new CharacterCreatorData(entityName , imageStruct , info , details , new Entities(entityName + ".CStf")));
				cstf.endList();
				
			}
			
			checks = new ByteBuffer[charactersToChooseFrom.size()];
			for(int i = 0 ; i < checks.length ; i ++) checks[i] = alloc0(allocator);
			cstf.endList();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} 
					
	}

	public void layout() {
		
		if(nk_begin(context , "Choose Your Warrior" , characterChooserRect , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
				
			cdNode<CharacterCreatorData> iter = charactersToChooseFrom.get(0);
			for(int i = 0 ; i < charactersToChooseFrom.size() ; i ++ , iter = iter.next) {
			
				nk_layout_row_dynamic(context , 40 , 1);
				if(nk_selectable_text(context , iter.val.name , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED , checks[i])){
			
					selectedCharacter = iter.val;
					//deselect all others
					for(ByteBuffer x : checks) if (x != checks[i]) put(x , false);
					defaultStats = new EntityRPGStats(iter.val.entity);
					((EntityRPGStats)iter.val.entity.components()[Entities.RPGOFF]).copy(defaultStats);
					
				}
				
				nk_layout_row_begin(context , NK_STATIC , iter.val.graphicInfo.height() / 2 , 2);
				int padding = (int) ((characterChooserRect.w() - iter.val.graphicInfo.width() / 2));
				nk_layout_row_push(context , padding / 2);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , iter.val.graphicInfo.width() / 2);
				nk_image(context , iter.val.graphic);
				
			}			
			
		}
		
		nk_end(context);
				
		if(nk_begin(context , "Character Sheet" , characterStatsRect , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
			
			//layout stats specific to each character
			if(selectedCharacter != null && selectedCharacter.entity.has(ECS.RPG_STATS)) {
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context, "Characteristic Points:" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context, "" + availablePoints , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
								
				EntityRPGStats stats = (EntityRPGStats)selectedCharacter.entity.components()[Entities.RPGOFF];
				
				stats.forValidStats((name , value) -> {
										
					if(value >= 0) {
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 4);
						nk_layout_row_push(context , 170);				
						nk_text(context , capitalize(name) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
						
						nk_layout_row_push(context , 50);
						nk_text(context , "" + value , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
						
						nk_layout_row_push(context , 35);
						if(nk_button_symbol(context , NK_SYMBOL_PLUS)) {
							
							if(availablePoints > 0) {
								
								stats.setCharacteristicForName(name, value + 1);
								availablePoints -= 1;
								
							}
							
						}
						
						nk_layout_row_push(context , 35);
						if(nk_button_symbol(context , NK_SYMBOL_MINUS)) {
							
							//ensure the value is not going lower than its default
							if(value > defaultStats.getCharacteristicForName(name)) {
								
								availablePoints += 1;
								stats.setCharacteristicForName(name, value - 1);
								
							}
							
						}
						
						nk_layout_row_end(context);
						
					}					
					
				});
				
			}			
			
		}
		
		nk_end(context);
		
		if(nk_begin(context , "Details" , characterDetailsRect , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
						
			if(selectedCharacter != null) {

				nk_layout_row_dynamic(context , 430 , 1);
				nk_text_wrap(context , selectedCharacter.details);
				
				nk_layout_row_dynamic(context , 50 , 1);
				if(nk_button_label(context , "Finish")) {
					
					Supplier<String> saveName = DialogUtils.newInputBox("Name This Save" , (1920/2) - 175, 540);					
					TemporalExecutor.onTrue(() -> saveName.get() != null , () -> newPlayer = new PlayerCharacter(saveName.get() , selectedCharacter.entity));
					
				}
				
			}
			
		}
		
		nk_end(context);
		
	}
	
	public PlayerCharacter newPlayer() {
		
		return newPlayer;
		
	}	
	
	public String startingLevel() {
		
		return startingLevel;
		
	}

	public String startingDoor() {
		
		return startingDoor;
		
	}
	
}
