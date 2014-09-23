package com.mygdx.crazysoccer;

import java.util.Map;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion; 
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.crazysoccer.Actions.Action;
import com.mygdx.crazysoccer.Actions.Controls;

public class Player extends Actor {
	
	private Field field;
	private Ball ball;
	
	// Идентификатор игрока (используется при определении нажатия управляющих кнопок)
	private int PLAYER_ID = 0;
		
	private static final int FRAME_COLS = 8;    
    private static final int FRAME_ROWS = 8;     
    
    // Спрайт для отрисовки персонажа
    public SpriteBatch spriteBatch;
    
    // Параметры спрайта
    private int SPRITE_WIDTH = 32;
    private int SPRITE_HEIGHT = 32;
    private float SPRITE_SCALE = 3.0f;
    
    // Размеры игрока с учетом масштабирования
    private float PLAYER_WIDTH = SPRITE_SCALE * SPRITE_WIDTH;
    private float PLAYER_HEIGHT = SPRITE_SCALE * SPRITE_HEIGHT;
    
    // Парамерты прыжка персонажа
    private float JUMP_HEIGHT = 0;
    
    // Масса персонажа
    private float MASS = 62.0f;
    
    // Сила персонажа
    private float STRENGTH = 500.0f;
    
    // Текущая скорость движения персонажа при прыжке 
    private float JUMP_VELOCITY = 0.0f;
    
    
    // Параметры персонажа
    private float CURENT_SPEED_X = 0.0f;
    private float CURENT_SPEED_Y = 0.0f;
    private float WALKING_SPEED = 3.0f;
    private float RUN_SPEED = 6.0f;
    private float POS_X = 0.0f;
    private float POS_Y = 0.0f;
    
    // Флаг принимает значение true если игрок контролирует мяч
    private boolean CATCH_BALL = false;
    
	// Перечень возможных состояний героя
	public static enum States {
		STAY,     			// Состояние покоя
		WALKING,			// Ходьба
		RUN,	  			// Бег 
		FATIGUE,  			// Усталость
		CELEBRATE,			// Празднование победы
		LEFT_HAND_KICK,		// Удар левой рукой
		FOOT_KICK, 			// Удар правой ногой
		JUMP,				// Прыжок
		SIT,				// Присел
		PASS				// Пасс
	} 
	
	
	// В какую сторону повернут персонаж
	public static enum Directions {
		RIGHT, LEFT
	}
	// Направление персонажа
	public Directions direction;
	
	// Слушатель ввода
	private Actions actionsListener;
    
    // Набор анимаций персонажа
    public Map<States, Animation> animations;
    
	// Кадры анимации покоя
	public TextureRegion[] stayFrames; 
	// Кадры анимации хотьбы
	public TextureRegion[] walkFrames; 
	// Кадры анимации бега
	public TextureRegion[] runFrames;
	// Кадры анимации приседания
	public TextureRegion[] sitFrames;
	// Кадры анимации праздования победы
	public TextureRegion[] passFrames;
	// Кадры анимации праздования победы
	public TextureRegion[] leftHandHitFrames;
	// Кадры анимации праздования победы
	public TextureRegion[] rightFootHitFrames;
	// Кадры анимации праздования победы
	public TextureRegion[] jumpFrames;
	
	// Текущий кадр анимации
	public TextureRegion currentFrame; 
	
	// Тень персонажа
	public Shadow shadow;
	
	public Texture animationSheet;
	public TextureRegion[][] animationMap;
    
	// Текущее состояние героя
	public Map<States, Boolean> state = new HashMap<States, Boolean>();
	
    public float stateTime = 0.0f; 
	
	public Player(int playerId) {
		super();
		
		this.PLAYER_ID = playerId;
		
		animations = new HashMap<States, Animation>();
		
        // Первоначальная инициализация состояний анимаций персонажа
        Do(States.STAY, true);
        
        // Изначальное направление персонажа
        direction = Directions.RIGHT;
		
        // Загрузка изображения с анимацией персонажа
        animationSheet = new Texture(Gdx.files.internal("kunio.png"));
        
        // Загрузка карты анимаций персонажа
        animationMap = TextureRegion.split(animationSheet, animationSheet.getWidth()/FRAME_COLS, animationSheet.getHeight()/FRAME_ROWS);
        
        // Спрайт для отрисовки персонажа
        spriteBatch = new SpriteBatch(); 
        
        // Создаем анимацию покоя
        stayFrames = new TextureRegion[1];
        stayFrames[0] = animationMap[0][0];
        animations.put(States.STAY, new Animation(1.0f, stayFrames));
        
        // Создаем анимацию ходьбы
        walkFrames = new TextureRegion[4];
        walkFrames[0] = animationMap[0][1];
        walkFrames[1] = animationMap[0][0];
        walkFrames[2] = animationMap[0][2];
        walkFrames[3] = animationMap[0][0];
        animations.put(States.WALKING, new Animation(0.13f, walkFrames));
        
        // Создаем анимацию бега
        runFrames = new TextureRegion[2];
        runFrames[0] = animationMap[1][4];
        runFrames[1] = animationMap[0][0];
        animations.put(States.RUN, new Animation(0.13f, runFrames));
        
        // Создаем анимацию приседания
        sitFrames = new TextureRegion[1];
        sitFrames[0] = animationMap[0][3];
        animations.put(States.SIT, new Animation(0.30f, sitFrames));
        
        // Создаем анимацию паса
        passFrames = new TextureRegion[2];
        passFrames[0] = animationMap[0][5];
        passFrames[1] = animationMap[0][6];
        animations.put(States.PASS, new Animation(0.25f, passFrames));
        
        // Создаем анимацию удара левой рукой
        leftHandHitFrames = new TextureRegion[3];
        leftHandHitFrames[0] = animationMap[1][2];
        leftHandHitFrames[1] = animationMap[1][1];
        leftHandHitFrames[2] = animationMap[1][2];
        animations.put(States.LEFT_HAND_KICK, new Animation(0.04f, leftHandHitFrames));
        
        // Создаем анимацию удара левой рукой
        rightFootHitFrames = new TextureRegion[11];
        rightFootHitFrames[0] = animationMap[0][7];
        rightFootHitFrames[1] = animationMap[0][7];
        rightFootHitFrames[2] = animationMap[0][7];
        rightFootHitFrames[3] = animationMap[1][0];
        rightFootHitFrames[4] = animationMap[1][1];
        rightFootHitFrames[5] = animationMap[1][1];
        rightFootHitFrames[6] = animationMap[1][1];
        rightFootHitFrames[7] = animationMap[1][1];
        rightFootHitFrames[8] = animationMap[1][1];
        rightFootHitFrames[9] = animationMap[1][1];
        rightFootHitFrames[10] = animationMap[1][1];
        animations.put(States.FOOT_KICK, new Animation(0.06f, rightFootHitFrames));
        
        // Создаем анимацию прыжка
        jumpFrames = new TextureRegion[1];
        jumpFrames[0] = animationMap[0][4];
        animations.put(States.JUMP, new Animation(0.2f, jumpFrames));
        
        shadow = new Shadow();
	}
	
	public void attachField(Field f) {
		this.field = f;
	}
	
	public void attachBall(Ball b) {
		this.ball = b;
	}
	
	public int getPlayerId() {
		return this.PLAYER_ID;
	}
	
	public float width() {
		return this.SPRITE_SCALE * this.SPRITE_WIDTH;
	}
	
	// Проверка необходимости зеркалирования спрайта персонажа
	private boolean getFlipX() {
		return (this.direction == Directions.LEFT);
	}
	
	// Возвращает текущую высоту прыжка персонажа
	public float jumpHeight() {
		return this.JUMP_HEIGHT;
	}
	
	public float getJumpVelocity() {
		return this.JUMP_VELOCITY;
	}
	
	public void setJumpVelocity(float v) {
		this.JUMP_VELOCITY = v;
	}
	
	public float getMass() {
		return this.MASS;
	}
	
	public void setMass(float m) {
		this.MASS = m;
	}
	
	public float getStrength() {
		return this.STRENGTH;
	}
	
	public void setStrength(float f) {
		this.STRENGTH = f;
	}
	
	public float getWidth() {
		return this.PLAYER_WIDTH;
	}
	
	public float getHeight() {
		return this.PLAYER_HEIGHT;
	}
	
	public float getAbsX() {
		return this.POS_X;
	}
	
	public float getAbsY() {
		return this.POS_Y;
	}
	
	public float getAbsH() {
		return this.JUMP_HEIGHT;
	}
	
	public void setAbsX(float x) {
		this.POS_X = x;
	}
	
	public void setAbsY(float y) {
		this.POS_Y = y;
	}
	
	public void setAbsH(float h) {
		this.JUMP_HEIGHT = h;
	}
	
	public void setActionsListener(Actions al) {
		this.actionsListener = al;
	}
	
	public void setVelocityX(float v) {
		this.CURENT_SPEED_X = v;
	}
	
	public void setVelocityY(float v) {
		this.CURENT_SPEED_Y = v;
	}
	
	public float getVelocityX() {
		return this.CURENT_SPEED_X;
	}
	
	public float getVelocityY() {
		return this.CURENT_SPEED_Y;
	}
	
	// Проверка может ли персонаж выполнить действие
	public boolean Can(States stateToCheck) {
		boolean isCan = false;
		
		switch (stateToCheck) {
		
			/* 
			 * Персонаж может идти если он:
			 * 	1. не бъет ногой
			 *  2. не бъет рукой
			 */
			case WALKING : 
				isCan = !state.get(States.LEFT_HAND_KICK)  && 
						!state.get(States.FOOT_KICK) &&
						!state.get(States.SIT) &&
						!state.get(States.JUMP);
			break;
			
			/* 
			 * Персонаж может бежать если он:
			 * 	1. не бъет ногой
			 *  2. не бъет рукой
			 */
			case RUN: 
				isCan = !state.get(States.LEFT_HAND_KICK)  && 
						!state.get(States.FOOT_KICK) &&
						!state.get(States.SIT) &&
						!state.get(States.JUMP);
			break;
			
			/* 
			 * Персонаж может выполнить удар рукой если он:
			 * 	1. не бъет рукой
			 *  2. не бъет ногой
			 */
			case LEFT_HAND_KICK: 
				isCan = !state.get(States.LEFT_HAND_KICK) && 
						!state.get(States.FOOT_KICK);
			break;
			
			/* 
			 * Персонаж может выполнить удар ногой если он:
			 * 	1. не дает пасс
			 *  2. не бьет ногой
			 */
			case PASS: 
				isCan = !state.get(States.FOOT_KICK) &&
						!state.get(States.PASS);
			break;
			
			/* 
			 * Персонаж может выполнить удар ногой если он:
			 * 	1. не дает пасс
			 *  2. не бьет ногой
			 */
			case FOOT_KICK: 
				isCan = !state.get(States.FOOT_KICK) && 
						!state.get(States.PASS);
			break;
			
			/* 
			 * Персонаж может прыгнуть если он:
			 * 	1. не бъет ногой
			 *  2. не бъет рукой
			 *  3. не находится в воздухе
			 */
			case JUMP: 
				isCan = !state.get(States.LEFT_HAND_KICK) && 
						!state.get(States.FOOT_KICK) &&
						!state.get(States.JUMP);
			break;
		}
		
		return isCan;
	}
	
	// Постановка задания на выполнение действия
	public void Do(States state, boolean stopAll) {
		// Если установлен флаг stopAll то останавливаем все анимации
		if (stopAll) this.stopAll();
		
		// Если нужно выполнить дополнительные действия
		switch (state) {
			case FOOT_KICK:
				disableDirections();
				actionsListener.disableAction(Controls.ACTION1, this.PLAYER_ID);
				this.stateTime = 0.0f;
				
				// Если при ударе ногой игрок не находится в воздухе то останавливаем его мгновенно
				if (this.JUMP_HEIGHT == 0) {
					this.CURENT_SPEED_X = 0.0f;
					this.CURENT_SPEED_Y = 0.0f;
				}
			break;
			
			case PASS:
				disableDirections();
				actionsListener.disableAction(Controls.ACTION2, this.PLAYER_ID);
				this.stateTime = 0.0f;
				
				// Если при ударе ногой игрок не находится в воздухе то останавливаем его мгновенно
				if (this.JUMP_HEIGHT == 0) {
					this.CURENT_SPEED_X = 0.0f;
					this.CURENT_SPEED_Y = 0.0f;
				}
			break;
			
			case LEFT_HAND_KICK: 
				disableDirections();
				actionsListener.disableAction(Controls.ACTION2, this.PLAYER_ID);
			break;
			
			case JUMP: 
				actionsListener.disableAction(Controls.ACTION3, this.PLAYER_ID);
				this.stateTime = 0.0f;
				this.setJumpVelocity(this.STRENGTH / this.MASS);
			break;
			
			case RUN: 
				this.CURENT_SPEED_X = this.RUN_SPEED;
			break;
			
			case STAY: 
				this.CURENT_SPEED_X = 0.0f;
				this.CURENT_SPEED_Y = 0.0f;
			break;
			
			case SIT: 
				this.stateTime = 0.0f;
				this.CURENT_SPEED_X = 0.0f;
				this.CURENT_SPEED_Y = 0.0f;
			break;
		}
		
		// Добавляем задачу на исполнение
		this.state.put(state, true);
	}
	
	// Отключение всех действий к перемещению персонажа
	private void disableDirections() {
		actionsListener.disableAction(Controls.RIGHT, this.PLAYER_ID);
		actionsListener.disableAction(Controls.LEFT, this.PLAYER_ID);
		actionsListener.disableAction(Controls.UP, this.PLAYER_ID);
		actionsListener.disableAction(Controls.DOWN, this.PLAYER_ID);
	}
	
	// Текущее состояние персонажа
	public States curentState() {
		for (int i = 0; i < States.values().length; i++) {
			if (state.get(States.values()[i])) {
				return States.values()[i];
			}
		}
		return States.STAY;
	}
	
	// Отключение действия
	private void disableAction(Action disableAction) {
		actionsListener.remove(disableAction);
	}
	
	// Полностью остановить игрока
	private void stopAll() {
		for (int i = 0; i < States.values().length; i++) {
			state.put(States.values()[i], false);
		}
	}
	
	private int currentFrame() {
		return animations.get(this.curentState()).getKeyFrameIndex(stateTime);
	}
	
	@Override
	public void act(float delta) {
		
		if (actionsListener.getActionStateFor(Controls.UP, this.PLAYER_ID).pressed) { 
			// Если персонаж не бежи, и нажата клавиша вверх
			if (curentState() != States.RUN && Can(States.WALKING)) {
				this.CURENT_SPEED_Y = this.WALKING_SPEED;
				Do(States.WALKING, true);
			}
			if (curentState() != States.JUMP && curentState() != States.SIT) {
				this.CURENT_SPEED_Y = this.WALKING_SPEED;
			}
		}
		
		if (actionsListener.getActionStateFor(Controls.DOWN, this.PLAYER_ID).pressed) { 
			// Если персонаж не бежи, и нажата клавиша вниз
			if (curentState() != States.RUN && Can(States.WALKING)) {
				this.CURENT_SPEED_Y = -this.WALKING_SPEED;
				Do(States.WALKING, true);
			}
			if (curentState() != States.JUMP && curentState() != States.SIT) {
				this.CURENT_SPEED_Y = -this.WALKING_SPEED;
			}
		}
		
		
		if (actionsListener.getActionStateFor(Controls.LEFT, this.PLAYER_ID).pressed) { 
			this.direction = Directions.LEFT;
			
			// Если было двойное нажатие кнопки, то делаем персонаж бегущим
			if (actionsListener.getActionStateFor(Controls.LEFT, this.PLAYER_ID).doublePressed && Can(States.RUN)) {
				Do(States.RUN, true);
			} 
			else if (Can(States.WALKING)) {
				this.CURENT_SPEED_X = -this.WALKING_SPEED;
				Do(States.WALKING, true);
			}
		}
		
		if (actionsListener.getActionStateFor(Controls.RIGHT, this.PLAYER_ID).pressed) { 
			this.direction = Directions.RIGHT;
			
			if (actionsListener.getActionStateFor(Controls.RIGHT, this.PLAYER_ID).doublePressed && Can(States.RUN)) {
				Do(States.RUN, true);
			} 
			else if (Can(States.WALKING)) {
				this.CURENT_SPEED_X = this.WALKING_SPEED;
				Do(States.WALKING, true);
			}
		}
		
		// Меняем вектор направления скорости в зависимости от направления движения персонажа
		if (this.CURENT_SPEED_X > 0) {
			if (this.direction == Directions.LEFT) { 
				 this.CURENT_SPEED_X = -this.CURENT_SPEED_X;
			}
		}
		else {
			if (this.direction == Directions.RIGHT) { 
				 this.CURENT_SPEED_X = -this.CURENT_SPEED_X;
			}
		}
		
		// Перемещение персонажа
		movePlayerBy(new Vector2(this.CURENT_SPEED_X, this.CURENT_SPEED_Y));
		
		
		// Если игрок находится в непосредственной близости возле мяча
		if (this.ballIsNear()) {
			
			// Если в настоящий момент игроком производится удар по мячу
			if (curentState() == States.FOOT_KICK && currentFrame() >= 2 && currentFrame() <= 4) {
				if (direction == Directions.RIGHT) 
					ball.kick(50, 90);
				else 
					ball.kick(50, 270);
				
				// Отмечаем что игрок потерял мяч
				this.catchBall(false);
			}
			// Если персонаж ничего не делает и мяч никем не контролируется
			else if (!ball.isCatched()) {
				// Отмечаем что игрок заполучил мяч
				this.catchBall(true);
				ball.setVelocityX(0);
			}
		}
		
		
		// Удар правой ногой
		if (actionsListener.getActionStateFor(Controls.ACTION1, this.PLAYER_ID).pressed && Can(States.FOOT_KICK)) {
			Do(States.FOOT_KICK, true);
		}
		
		// Пас
		if (actionsListener.getActionStateFor(Controls.ACTION2, this.PLAYER_ID).pressed && Can(States.PASS)) {
			Do(States.PASS, true);
		}
		
		// Прыжок
		if (actionsListener.getActionStateFor(Controls.ACTION3, this.PLAYER_ID).pressed && Can(States.JUMP)) {
			Do(States.JUMP, true);
		}
		
		// Если ниодна из кнопок направления движения не нажата
		if (!actionsListener.getActionStateFor(Controls.UP, this.PLAYER_ID).pressed && 
			!actionsListener.getActionStateFor(Controls.DOWN, this.PLAYER_ID).pressed &&
			!actionsListener.getActionStateFor(Controls.LEFT, this.PLAYER_ID).pressed &&
			!actionsListener.getActionStateFor(Controls.RIGHT, this.PLAYER_ID).pressed &&
			!actionsListener.getActionStateFor(Controls.ACTION1, this.PLAYER_ID).pressed &&
			!actionsListener.getActionStateFor(Controls.ACTION2, this.PLAYER_ID).pressed &&
			!actionsListener.getActionStateFor(Controls.ACTION3, this.PLAYER_ID).pressed) {
			
			// Если анимация была WALKING то отключаем ее
			if (state.get(States.WALKING)) {
				Do(States.STAY, true);
			}
		}
		
		// Если персонаж бежит и не нажата кнопка
		if (!actionsListener.getActionStateFor(Controls.UP, this.PLAYER_ID).pressed && 
			!actionsListener.getActionStateFor(Controls.DOWN, this.PLAYER_ID).pressed &&
			 state.get(States.RUN)) 
		{
			this.CURENT_SPEED_Y = 0.0f;
		}
		
		// Реализация гравитации
		if (curentState() == States.JUMP || this.JUMP_HEIGHT > 0) {
			this.JUMP_VELOCITY -= 18.0f * Gdx.graphics.getDeltaTime();
			this.JUMP_HEIGHT += this.JUMP_VELOCITY;
			
			if (this.JUMP_HEIGHT <= 0) {
				Do(States.SIT, true);
				this.JUMP_HEIGHT = 0.0f;
			}
		}
		
		//if (this.PLAYER_ID == 0) System.out.println("Player X / Y / H:"+POS_X+" / "+POS_Y+" / "+JUMP_HEIGHT);
	}
	
	
	// Находится ли мяч рядом
	public boolean ballIsNear() {
		return Math.abs(ball.getAbsX() - this.getAbsX()) <= 40 && 
				ball.getAbsY() - this.getAbsY() >= -40 && 
				ball.getAbsY() - this.getAbsY() <= 20 &&
				ball.getAbsH() + ball.getDiameter() > this.getAbsH() && 
				ball.getAbsH() < this.getAbsH() + this.getHeight();
	}
	
	private void movePlayerBy(Vector2 movePoint) {
		
		boolean doStop = false;
		
		if (this.POS_X >= 0 && this.POS_X <= field.worldWidth && this.POS_Y >= 0 && this.POS_Y <= field.worldHeight) {
			
			// Ограничение выхода персонажа за пределы поля
			if (this.POS_X + movePoint.x < 0) {
				movePoint.x = -this.POS_X;
				doStop = true;
			}
			else if (this.POS_X + movePoint.x > field.worldWidth) { 
				movePoint.x = field.worldWidth - this.POS_X;
				doStop = true;
			}
			
			if (this.POS_Y + movePoint.y < 0) {
				movePoint.y = -this.POS_Y;
				doStop = true;
			}
			else if (this.POS_Y + movePoint.y > field.worldHeight) { 
				movePoint.y = field.worldHeight - this.POS_Y;
				doStop = true;
			}
			
			// Если персонаж достик границы игрового мира, то останавливаем его
			if (doStop) Do(States.STAY,true);
			
			this.POS_X += movePoint.x;
			this.POS_Y += movePoint.y;
			
			
			// Если игрок владеет мячом то привязываем перемещение мяча к этом игроку
			if (this.catchBall()) {
//				ball.setVelocityX(this.getVelocityX());
//				ball.setVelocityY(this.getVelocityY());
				
				if (this.direction == Directions.RIGHT) {
					ball.moveBallBy(new Vector2(this.getAbsX()-ball.getAbsX() + 33, this.getAbsY()-ball.getAbsY()-1));
				}
				else {
					ball.moveBallBy(new Vector2(this.getAbsX()-ball.getAbsX() - 33, this.getAbsY()-ball.getAbsY()-1));
				}
				ball.setAbsH(this.getAbsH());
			}
		}
	}
	
	
	// Контролирует ли мяч игрок
	public boolean catchBall() {
		return this.CATCH_BALL;
	}
	
	public void catchBall(boolean c) {
		ball.isCatched(c);
		this.CATCH_BALL = c;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		stateTime += Gdx.graphics.getDeltaTime();
		
		// Анимирование персонажа
		animations.get(this.curentState()).setPlayMode(PlayMode.LOOP);
		currentFrame = animations.get(this.curentState()).getKeyFrame(stateTime, true); 
		
		// Если окончено текущее действие
		if (animations.get(this.curentState()).isAnimationFinished(stateTime)) {
			if (this.curentState() != States.RUN && this.curentState() != States.JUMP) {
				Do(States.STAY, true);
			}
		}

        spriteBatch.begin();
        spriteBatch.draw(
    		currentFrame.getTexture(), 
    		this.getX() - this.width() / 2.0f, 
    		this.getY() + this.JUMP_HEIGHT, 
    		0, 
    		0, 
    		this.SPRITE_WIDTH, 
    		this.SPRITE_HEIGHT, 
    		this.SPRITE_SCALE, 
    		this.SPRITE_SCALE, 
    		0, 
    		currentFrame.getRegionX(), 
    		currentFrame.getRegionY(), 
    		this.SPRITE_WIDTH, 
    		this.SPRITE_HEIGHT, 
    		this.getFlipX(), 
    		false
		);
        spriteBatch.end();
        
        // Ресование тени персонажа
        shadow.setX(getX() - 10);
        shadow.setY(getY());
        shadow.setVisibility(this.JUMP_HEIGHT > 0);
//        shadow.draw();
	}
}