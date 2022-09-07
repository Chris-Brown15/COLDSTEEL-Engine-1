package Game.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import CSUtil.CSTFParser;
import Core.GameFiles;
import Core.Entities.Entities;
import Game.Levels.LevelLoadDoors;
import Game.Levels.Levels;

/**
 * Class wrapping the player's entity. This class is intended to be very light and entity agnostic. Pass this around 
 * for player-specific operations. There will not be a player component.
 * <br><br>
 * When we want to save or load the game, we will save the entity's current state along with its position in the game world 
 * as a separate .CStf file within saves.
 * 
 */
public class PlayerCharacter implements GameFiles<PlayerCharacter> {

	private Entities playerEntity;
	private String saveName;
	private int nextSave = 0;
	private LevelLoadDoors previouslyUsedLoadDoor;
	
	public PlayerCharacter(String saveName , Entities playersEntity) {
		
		this.saveName = saveName;
		this.playerEntity = playersEntity;
		
	}

	public PlayerCharacter() {
				
		playerEntity = new Entities();
		
	}

	public void moveTo(float x , float y) {
		
		playerEntity.moveTo(x , y);
		
	}
	
	public void moveTo(float[] xAndY) {
		
		playerEntity.moveTo(xAndY);
		
	}
	
	public Entities playersEntity() {
	
		return playerEntity;
		
	}	

	/**
	 * Important, this does not represent the name of the in-game character the player is playing as, but rather the name of the 
	 * save they gave when they created their character.
	 * 
	 * @return playerName
	 */
	public String playerCharacterName() {
		
		return saveName;
		
	}
	
	public void nextSave(int nextSave) {
		
		this.nextSave = nextSave;
		
	}
	
	public void previouslyUsedLoadDoor(LevelLoadDoors door) {
		
		this.previouslyUsedLoadDoor = door;
		
	}
	
	public LevelLoadDoors previouslyUsedLoadDoor() {
		
		return previouslyUsedLoadDoor;
		
	}
	
	
	
	@Override public void delete() {}

	@Override public void write(Object...additionalData) {
		
		if(!Files.exists(Paths.get(CS.COLDSTEEL.data + "saves/" + saveName + "/"))){
			
			try {
				
				Files.createDirectory(Paths.get(CS.COLDSTEEL.data + "saves/" + saveName + "/"));
				
			} catch (IOException e) {

				e.printStackTrace();
				
			}
			
		}
		
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(CS.COLDSTEEL.data + "saves/" + saveName + "/" + saveName + nextSave + ".CStf"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			
			cstf.wname(saveName);
			playerEntity.write(writer);
			
			cstf.wlist("location");
			Levels currentLevel = (Levels)additionalData[0];
			cstf.wlabelValue("level" , currentLevel.macroLevel() + "/" + currentLevel.gameName());
			cstf.wlabelValue("position", playerEntity.getMidpoint());
			cstf.endList();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void load(String filepath) {
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))){
			
			saveName = reader.readLine();
			nextSave = Character.getNumericValue(filepath.charAt(filepath.length() - 6));
			playerEntity.load(reader);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer , Object...data) {}

	@Override public void load(BufferedReader reader) throws IOException {
		
		saveName = reader.readLine();		
		playerEntity.load(reader);
		
		
	}
	
}