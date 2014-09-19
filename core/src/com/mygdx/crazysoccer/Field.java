package com.mygdx.crazysoccer;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.crazysoccer.Wind.WindDirections;

public class Field extends Stage {
	
	private Stage stage;
	
	// Екземпляр класса описывающего игрока
	private Player[] players = new Player[2];
	
	// Листья
	private Leaf[] leafs = new Leaf[30];
	
	// Четыре точки определяющие размеры поля
	private Vector2 leftBottom;
	private Vector2 rightBottom;
	private Vector2 rightTop;
	private Vector2 leftTop;
	
	// Размры игрового (футбольного) поля
	public int fieldMinWidth;
	public int fieldMaxWidth;
	public int fieldHeight;
	
	// Размеры штрафной
	int innerBoxWidth = 130;
	int innerBoxHeight = 150;
	int outerBoxWidth = 300;
	int outerBoxHeight = 280;
	
	// Параметры игрового экрана
	public float camMaxX;
	public float camMaxY;
	public float worldWidth;
	public float worldHeight;
	
	// Смещение игрового поля относительно карты (левого нижнего угла)
	public int fieldOffsetX;
	public int fieldOffsetY;
	
	public TiledMap fieldMap;
    public TiledMapRenderer fieldMapRenderer;
    public static OrthographicCamera camera;
    
    public ShapeRenderer shapeRenderer;
	
    // Сохранение нажатых клавиш и их времени
    public Actions actions = new Actions();
	
	public Field(ScreenViewport screenViewport) {
		super(screenViewport);
		
		for (int i = 0; i < players.length; i++) {
			// Создание первого игрока
			players[i] = new Player(i+1);
			// Привязка слушателя ввода для игрока
			players[i].setActionsListener(actions);
			// Добавление игрока (актера) на сцену (поле)
			this.addActor(players[i]);
			
			players[i].attachField(this);
		}
		
		// Создание листов
		for (int i = 0; i < leafs.length; i++) {
			leafs[i] = new Leaf(WindDirections.RIGHT_LEFT);
			leafs[i].setPosition(
				(float)Math.random() * Gdx.graphics.getWidth(), 
				(float)Math.random() * Gdx.graphics.getHeight()
			);
			
			this.addActor(leafs[i]);
		}
		
		// Для отрисовки линий поля
		shapeRenderer = new ShapeRenderer();
		
		// Создание камеры
		camera = new OrthographicCamera(Vars.WINDOW_WIDTH, Vars.WINDOW_HEIGHT);
		
        camera.update();
	}
	
	public void LoadMap(String mapName) {
		fieldMap = new TmxMapLoader().load(mapName);
		fieldMapRenderer = new OrthogonalTiledMapRenderer(fieldMap, 1.0f);
		
		
		for (int i = 0; i < fieldMap.getLayers().get("field").getObjects().getCount(); i++) {
        	MapObject ma = (MapObject)fieldMap.getLayers().get("field").getObjects().get(i);
        	
        	if ((Object)ma instanceof PolylineMapObject) {
        		Polyline polyline = ((PolylineMapObject)ma).getPolyline();
        		
        		// Получение опорных точек трапеции поля
        		leftBottom  = new Vector2(polyline.getVertices()[0],polyline.getVertices()[1]);
        		rightBottom = new Vector2(polyline.getVertices()[2],polyline.getVertices()[3]);
        		rightTop    = new Vector2(polyline.getVertices()[4],polyline.getVertices()[5]);
        		leftTop     = new Vector2(polyline.getVertices()[6],polyline.getVertices()[7]);
        		
        		Arrays.sort(polyline.getVertices());
        		
        		// Получение размеров игрового поля
        		fieldMinWidth = (int)polyline.getVertices()[9] - (int)polyline.getVertices()[0];
        		fieldMaxWidth = (int)polyline.getVertices()[8] - (int)polyline.getVertices()[5];
        		fieldHeight   = (int)polyline.getVertices()[6] - (int)polyline.getVertices()[0];
        		
        		// Получение смещения разметки поля относительно карты
        		fieldOffsetX = Math.round(ma.getProperties().get("x", Float.class));
        		fieldOffsetY = Math.round(ma.getProperties().get("y", Float.class));
        		
        		camera.position.set(Gdx.graphics.getWidth() / 2.0f, fieldOffsetY + fieldHeight / 2.0f, 0);
        		
        		// Определение наибольшей координаты X, в которую можно смещать камеру     
        		camMaxX = fieldMaxWidth + 4 * fieldOffsetX - Gdx.graphics.getWidth() / 2.0f;
        		camMaxY = fieldHeight + 2 * fieldOffsetY - Gdx.graphics.getHeight() / 2.0f;
        		
        		// Ширина игрового мира. Подсчет ведется с учетом того, что смещение справа такое же как и слева
        		worldWidth = fieldMaxWidth + 2 * fieldOffsetX; 
        		worldHeight = fieldHeight + 2 * fieldOffsetY - 10; 
        		
        		players[0].setX(100);
        		players[0].setY(Gdx.graphics.getHeight() / 2.0f);
        		players[0].POS_X = 100;
        		players[0].POS_Y = fieldOffsetY + fieldHeight / 2.0f;
        		
        		players[1].setX(100);
        		players[1].setY(Gdx.graphics.getHeight() / 2.0f);
        		players[1].POS_X = 200;
        		players[1].POS_Y = fieldOffsetY + fieldHeight / 2.0f;
        		
//        		for (int h = 0; h < fieldHeight; h++) {
//        			System.out.println(h+" "+mGetSideLineProjection(h));
//        		}
        	}
		}
	}
	
	public void moveCamera() {
		// Перемещение всех персонажей относительно камеры
		players[1].setX(players[1].POS_X - camera.position.x + Gdx.graphics.getWidth() / 2.0f);
		players[1].setY(players[1].POS_Y - camera.position.y + Gdx.graphics.getHeight() / 2.0f);
	}
	
	// Получение длины проекции отрезка на ось аута поля (используется для проверки
	// находится ли объект в пределах поля)
	private float mGetSideLineProjection(int h) {
		float b = (fieldMaxWidth - fieldMinWidth) / 2.0f;
		float c = (float)Math.sqrt(fieldHeight * fieldHeight + b * b);
		float sinAlpha = (float)fieldHeight / c;
		float cosAlpha = (float)b / c;
		
		return Math.abs(h / (sinAlpha / cosAlpha));
	}
	
	public String printFieldSizes() {
		return "Min width:"+fieldMinWidth+"\n"+"Max width:"+fieldMaxWidth+"\n"+"Height:"+fieldHeight;
	}
	
	
	public void resize (int width, int height) {
	    // See below for what true means.
	    stage.getViewport().update(width, height, true);
	}
	
	// Отрисовка поля
	public void drawField() {
		 Gdx.gl20.glLineWidth(6);
		 shapeRenderer.setProjectionMatrix(camera.combined);
		 shapeRenderer.begin(ShapeType.Line);
		 shapeRenderer.setColor(1, 1, 1, 1);
		 
		 // Рамка поля
		 shapeRenderer.line(leftBottom.x + fieldOffsetX, leftBottom.y + fieldOffsetY, rightBottom.x + fieldOffsetX, rightBottom.y + fieldOffsetY);
		 shapeRenderer.line(rightBottom.x + fieldOffsetX, rightBottom.y + fieldOffsetY, rightTop.x + fieldOffsetX, rightTop.y + fieldOffsetY);
		 shapeRenderer.line(rightTop.x + fieldOffsetX, rightTop.y + fieldOffsetY, leftTop.x + fieldOffsetX, leftTop.y + fieldOffsetY);
		 shapeRenderer.line(leftTop.x + fieldOffsetX, leftTop.y + fieldOffsetY, leftBottom.x + fieldOffsetX, leftBottom.y + fieldOffsetY);
		 
		 // Центр поля
		 shapeRenderer.line(leftBottom.x + fieldMaxWidth / 2.0f + fieldOffsetX, leftBottom.y + fieldOffsetY, leftBottom.x + fieldMaxWidth / 2.0f + fieldOffsetX, leftTop.y + fieldOffsetY);
		 
		 // Круг в центре поля
		 shapeRenderer.circle(leftBottom.x + fieldMaxWidth / 2.0f + fieldOffsetX, leftBottom.y + fieldHeight / 2.0f + fieldOffsetY, 200);
		 
		 // Кружки для подачи угловых
		 shapeRenderer.arc(leftBottom.x + fieldOffsetX, leftBottom.y + fieldOffsetY, 60, 0, 88);
		 shapeRenderer.arc(leftTop.x + fieldOffsetX, leftTop.y + fieldOffsetY, 60, -91, 91);
		 shapeRenderer.arc(rightTop.x + fieldOffsetX, rightTop.y + fieldOffsetY, 60, 180, 91);
		 shapeRenderer.arc(rightBottom.x + fieldOffsetX, rightBottom.y + fieldOffsetY, 60, 92, 88);
		 
		 // Левые ворота
		 float yCenter = leftBottom.y + fieldHeight / 2.0f + fieldOffsetY;
		 shapeRenderer.line(leftBottom.x + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter + innerBoxHeight)), yCenter + innerBoxHeight, innerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter + innerBoxHeight)), yCenter + innerBoxHeight);
		 shapeRenderer.line(leftBottom.x + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter - innerBoxHeight)), yCenter - innerBoxHeight, innerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter - innerBoxHeight)), yCenter - innerBoxHeight);
		 shapeRenderer.line(innerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter + innerBoxHeight)), yCenter + innerBoxHeight, innerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter - innerBoxHeight)), yCenter - innerBoxHeight);
		 shapeRenderer.line(leftBottom.x + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter + outerBoxHeight)), yCenter + outerBoxHeight, outerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter + outerBoxHeight)), yCenter + outerBoxHeight);
		 shapeRenderer.line(leftBottom.x + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter - outerBoxHeight)), yCenter - outerBoxHeight, outerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter - outerBoxHeight)), yCenter - outerBoxHeight);
		 shapeRenderer.line(outerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter + outerBoxHeight)), yCenter + outerBoxHeight, outerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter - outerBoxHeight)), yCenter - outerBoxHeight);
		 shapeRenderer.arc(outerBoxWidth + fieldOffsetX + mGetSideLineProjection(Math.round(yCenter)), yCenter, 100, 268.5f, 180);
		 
		 // Правые ворота
		 shapeRenderer.line(rightBottom.x - mGetSideLineProjection(Math.round(yCenter + innerBoxHeight)) + fieldOffsetX, yCenter + innerBoxHeight, rightBottom.x - mGetSideLineProjection(Math.round(yCenter + innerBoxHeight)) - innerBoxWidth + fieldOffsetX, yCenter + innerBoxHeight);
		 shapeRenderer.line(rightBottom.x - mGetSideLineProjection(Math.round(yCenter - innerBoxHeight)) + fieldOffsetX, yCenter - innerBoxHeight, rightBottom.x - mGetSideLineProjection(Math.round(yCenter - innerBoxHeight)) - innerBoxWidth + fieldOffsetX, yCenter - innerBoxHeight);
		 shapeRenderer.line(rightBottom.x - mGetSideLineProjection(Math.round(yCenter + innerBoxHeight)) - innerBoxWidth + fieldOffsetX, yCenter + innerBoxHeight, rightBottom.x - mGetSideLineProjection(Math.round(yCenter - innerBoxHeight)) - innerBoxWidth + fieldOffsetX, yCenter - innerBoxHeight);
		 shapeRenderer.line(rightBottom.x - mGetSideLineProjection(Math.round(yCenter + outerBoxHeight)) + fieldOffsetX, yCenter + outerBoxHeight, rightBottom.x - mGetSideLineProjection(Math.round(yCenter + outerBoxHeight)) - outerBoxWidth + fieldOffsetX, yCenter + outerBoxHeight);
		 shapeRenderer.line(rightBottom.x - mGetSideLineProjection(Math.round(yCenter - outerBoxHeight)) + fieldOffsetX, yCenter - outerBoxHeight, rightBottom.x - mGetSideLineProjection(Math.round(yCenter - outerBoxHeight)) - outerBoxWidth + fieldOffsetX, yCenter - outerBoxHeight);
		 shapeRenderer.line(rightBottom.x - mGetSideLineProjection(Math.round(yCenter + outerBoxHeight)) - outerBoxWidth + fieldOffsetX, yCenter + outerBoxHeight, rightBottom.x - mGetSideLineProjection(Math.round(yCenter - outerBoxHeight)) - outerBoxWidth + fieldOffsetX, yCenter - outerBoxHeight);
		 shapeRenderer.arc(rightBottom.x - mGetSideLineProjection(Math.round(yCenter)) - outerBoxWidth + fieldOffsetX, yCenter, 100, 91.0f, 180);
		 
		 shapeRenderer.end();
		 
		 // Точка в центра поля
		 shapeRenderer.begin(ShapeType.Filled);
		 shapeRenderer.circle(fieldOffsetX + fieldMaxWidth / 2.0f, fieldOffsetX + fieldHeight / 2.0f, 15);
		 shapeRenderer.end();
	}
	
	public void processGame() { 
		drawField();
		
		// Изменение силы ветра
		if (Math.random() > 0.99f) {
			float windVelocity = (float)Math.random() * 25 + 5;
			
			for (int i = 0; i < leafs.length; i++) {
				leafs[i].setWindVelocity(windVelocity);
				
				if (windVelocity / 40.0f >= Math.random() || windVelocity / 40.0f >= Math.random()) {
					leafs[i].setWindDirection(WindDirections.LEFT_RIGHT);
				} 
				else {
					leafs[i].setWindDirection(WindDirections.NONE);
				}
			}
		}
		
		
		// Произвольное изменение направления ветра
		if (Math.random() > 0.999f) {
			int j = (int)Math.round(Math.random() * WindDirections.values().length);
			if (j >= WindDirections.values().length) j = WindDirections.values().length - 1;
			for (int i = 0; i < leafs.length; i++) {
				leafs[i].setWindDirection(WindDirections.values()[j]);
			}
		}
		
		
		// Сортировка спрайтов по z-index
		if (players[0].POS_Y > players[1].POS_Y) {
			players[0].setZIndex(1);
			players[1].setZIndex(2);
		}
		else {
			players[0].setZIndex(2);
			players[1].setZIndex(1);
		}
	}

	public void dispose() {
	    stage.dispose();
	}
	
	@Override
	public boolean keyUp(int keycode) {
		switch (keycode)
		{
			// Кнопки управления первым игроком
			case Keys.UP: //UP
				actions.remove(Actions.Action.UP_1);
			break;
			
			case Keys.DOWN: //DOWN
				actions.remove(Actions.Action.DOWN_1);
			break;
				
			case Keys.LEFT: //LEFT
				actions.remove(Actions.Action.LEFT_1);
			break;
				
			case Keys.RIGHT: //RIGHT
				actions.remove(Actions.Action.RIGHT_1);
			break;
			
			case Keys.Q:
				actions.remove(Actions.Action.ACTION1_1);
			break;
			
			case Keys.W:
				actions.remove(Actions.Action.ACTION2_1);
			break;
			
			case Keys.E:
				actions.remove(Actions.Action.ACTION3_1);
			break;
			
			
			
			case Keys.A:
				actions.remove(Actions.Action.ACTION1_2);
			break;
			
			case Keys.S:
				actions.remove(Actions.Action.ACTION2_2);
			break;
			
			case Keys.D:
				actions.remove(Actions.Action.ACTION3_2);
			break;
			
			case Keys.NUMPAD_4:
				actions.remove(Actions.Action.LEFT_2);
			break;
			
			case Keys.NUMPAD_6:
				actions.remove(Actions.Action.RIGHT_2);
			break;
			
			case Keys.NUMPAD_5:
				actions.remove(Actions.Action.DOWN_2);
			break;
			
			case Keys.NUMPAD_8:
				actions.remove(Actions.Action.UP_2);
			break;
		}
		
//		actions.debug();
		return false;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		
		switch (keycode)
		{
			// Кнопки управления первым игроком
			case Keys.UP: //UP
				actions.add(Actions.Action.UP_1);
			break;
			
			case Keys.DOWN: //DOWN
				actions.add(Actions.Action.DOWN_1);
			break;
				
			case Keys.LEFT: //LEFT
				actions.add(Actions.Action.LEFT_1);
			break;
				
			case Keys.RIGHT: //RIGHT
				actions.add(Actions.Action.RIGHT_1);
			break;
			
			case Keys.Q:
				actions.add(Actions.Action.ACTION1_1);
			break;
			
			case Keys.W:
				actions.add(Actions.Action.ACTION2_1);
			break;
			
			case Keys.E:
				actions.add(Actions.Action.ACTION3_1);
			break;
			
			
			// Кнопки управления вторым игроком
			case Keys.A:
				actions.add(Actions.Action.ACTION1_2);
			break;
			
			case Keys.S:
				actions.add(Actions.Action.ACTION2_2);
			break;
			
			case Keys.D:
				actions.add(Actions.Action.ACTION3_2);
			break;
			
			case Keys.NUMPAD_4:
				actions.add(Actions.Action.LEFT_2);
			break;
			
			case Keys.NUMPAD_6:
				actions.add(Actions.Action.RIGHT_2);
			break;
			
			case Keys.NUMPAD_5:
				actions.add(Actions.Action.DOWN_2);
			break;
			
			case Keys.NUMPAD_8:
				actions.add(Actions.Action.UP_2);
			break;
		}
		
//		actions.debug();
		return false;
	}
}