package com.example.starbound

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.graphics.*
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.Choreographer
import kotlin.math.*

class GameActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; window.decorView.systemUiVisibility=5894 or 1024 or 512; setContentView(GameView(this)) }
}

private class GameView(ctx:Context):View(ctx), Choreographer.FrameCallback {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private var last=0L; private var cam=0f; private var px=100f; private var py=0f; private var vx=0f; private var vy=0f; private var ground=0f; private var jumping=false
    private var screen=0; private var level=0; private var score=0; private var lives=3; private var paused=false; private val prefs=ctx.getSharedPreferences("save",0)
    private var left=false; private var right=false; private var jump=false; private var run=false
    private val blocks=mutableListOf<RectF>(); private val coins=mutableListOf<PointF>(); private val enemies=mutableListOf<RectF>(); private val pipes=mutableListOf<RectF>()
    init { level=prefs.getInt("level",0); score=prefs.getInt("score",0); buildLevel(); isFocusable=true; Choreographer.getInstance().postFrameCallback(this) }
    private fun buildLevel(){ blocks.clear(); coins.clear(); enemies.clear(); pipes.clear(); val step=94f
      if(level==0){ for(i in 0..68) if(i !in 16..18 && i !in 41..43) blocks.add(RectF(i*step,0f,i*step+step,40f)); for(x in listOf(500f,600f,1050f,1400f,1900f,2400f,3000f,3500f,4100f,4600f,5100f,5650f)) coins.add(PointF(x,-100f)); for(x in listOf(850f,2120f,3250f,4700f,5400f)) enemies.add(RectF(x,-42f,x+46,-2f)); for((x,h) in listOf(720f to 100f,1650f to 145f,2700f to 105f,3850f to 160f,5000f to 120f)) pipes.add(RectF(x,-h,x+100,0f)) }
      else { for(i in 0..64) if(i !in 12..14 && i !in 28..31 && i !in 47..50) blocks.add(RectF(i*step,0f,i*step+step,40f)); for(x in 450..5500 step 310) coins.add(PointF(x.toFloat(),-140f)); for(x in listOf(950f,1850f,3450f,4550f)) enemies.add(RectF(x,-42f,x+46,-2f)); for((x,h) in listOf(800f to 110f,2200f to 190f,3900f to 125f,5200f to 180f)) pipes.add(RectF(x,-h,x+100,0f)) }
    }
    override fun onDraw(c:Canvas){ super.onDraw(c); val w=width.toFloat(); val h=height.toFloat(); ground=h*.78f; c.save(); c.scale(w/1200f,h/680f)
      p.color=Color.rgb(99,190,255); c.drawColor(p.color); // sky
      // clouds
      for(i in 0..9){ val x=((i*290-cam*.18f)%1500+1500)%1500-100; p.color=Color.WHITE; c.drawCircle(x.toFloat(),100f+(i%3)*44,35f,p); c.drawCircle(x+35f,92f+(i%3)*44,43f,p); c.drawCircle(x+70f,105f+(i%3)*44,30f,p) }
      c.save(); c.translate(-cam,0f); val gy=530f
      // ground tiles
      p.color=Color.rgb(157,91,45); c.drawRect(0f,gy,7000f,680f,p); p.color=Color.rgb(65,190,70); c.drawRect(0f,gy,7000f,gy+12,p)
      for(b in blocks){ p.color=Color.rgb(228,139,51); c.drawRect(b.left,gy-44,b.right,gy,p); p.color=Color.rgb(255,190,94); c.drawRect(b.left+4,gy-40,b.right-4,gy-6,p); p.color=Color.rgb(98,63,43); c.drawRect(b.left+8,gy-19,b.left+13,gy-14,p) }
      for(q in pipes){ p.color=Color.rgb(18,150,65); c.drawRect(q.left,gy+q.top,q.right,gy-2,p); p.color=Color.rgb(53,220,82); c.drawRect(q.left-10,gy+q.top,q.right+10,gy+q.top+24,p) }
      for(e in enemies){ p.color=Color.rgb(138,66,29); c.drawOval(e.left,gy+e.top,e.right,gy+e.bottom+40,p); p.color=Color.WHITE; c.drawCircle(e.left+14,gy+e.top+17,5f,p); c.drawCircle(e.left+32,gy+e.top+17,5f,p); p.color=Color.BLACK; c.drawCircle(e.left+15,gy+e.top+17,2f,p); c.drawCircle(e.left+33,gy+e.top+17,2f,p) }
      for(co in coins){ p.color=Color.rgb(255,210,25); c.drawOval(co.x-13,gy+co.y-19,co.x+13,gy+co.y+19,p); p.color=Color.rgb(255,244,130); c.drawRect(co.x-3,gy+co.y-12,co.x+3,gy+co.y+12,p) }
      p.color=Color.LTGRAY; c.drawRect(6200f,gy-310,6210f,gy,p); p.color=Color.GREEN; val flag=Path().apply{moveTo(6210f,gy-310);lineTo(6300f,gy-275);lineTo(6210f,gy-240);close()}; c.drawPath(flag,p); p.color=Color.rgb(200,90,50); c.drawRect(6520f,gy-160,6670f,gy,p); p.color=Color.rgb(80,40,50); c.drawRect(6570f,gy-70,6620f,gy,p)
      p.color=Color.rgb(255,73,42); c.drawRoundRect(px,gy+py-55,px+40,gy+py,10f,10f,p); p.color=Color.rgb(255,220,155); c.drawCircle(px+26,gy+py-45,9f,p); c.restore(); c.restore()
      p.color=Color.WHITE; p.textSize=30f; c.drawText("WORLD ${level+1}     SCORE $score     LIVES $lives",30f,40f,p)
      drawButton(c,45f,h*.76f,105f,"◀"); drawButton(c,165f,h*.76f,105f,"▶"); drawButton(c,w-245,h*.76f,95f,"B"); drawButton(c,w-125,h*.76f,95f,"A")
      if(screen==0){ overlay(c,"STARBOUND RUNNER","Tap to play  •  Original retro platform adventure") }; if(paused) overlay(c,"PAUSED","Tap anywhere to resume"); if(screen==2) overlay(c,"LEVEL CLEAR!","Tap to continue")
    }
    private fun drawButton(c:Canvas,x:Float,y:Float,size:Float,label:String){p.color=0x88404050.toInt(); c.drawCircle(x+size/2,y+size/2,size/2,p); p.color=Color.WHITE;p.textSize=32f;c.drawText(label,x+size*.33f,y+size*.66f,p)}
    private fun overlay(c:Canvas,title:String,sub:String){p.color=0xBB08152F.toInt(); c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=52f;c.drawText(title,width/2f,height*.43f,p);p.textSize=24f;c.drawText(sub,width/2f,height*.53f,p);p.textAlign=Paint.Align.LEFT}
    override fun doFrame(t:Long){ if(last==0L)last=t; val dt=((t-last)/1e9f).coerceIn(0f,.04f); last=t; if(screen==1 && !paused) update(dt); invalidate(); Choreographer.getInstance().postFrameCallback(this) }
    private fun update(dt:Float){ val accel=if(run)1100f else 720f; if(left)vx-=accel*dt;if(right)vx+=accel*dt;if(!left&&!right)vx*=.82f;vx=vx.coerceIn(-300f,if(run)480f else 310f);px+=vx*dt;vy+=1450f*dt;py+=vy*dt
      if(py>0){py=0f;vy=0f;jumping=false}; val worldx=px+cam
      for(b in blocks) if(worldx+40>b.left&&worldx<b.right&&py>=-45&&py<0&&vy>0){py=0f;vy=0f}
      val it=coins.iterator(); while(it.hasNext()){val q=it.next();if(abs(worldx-q.x)<48&&abs(py-q.y)<60){score+=100;it.remove()}}
      for(e in enemies){ if(abs(worldx-(e.left+23))<35&&abs(py)<46){if(vy>100){score+=200;e.offset(9000f,0f)}else{lives--;px=100f;cam=0f;if(lives<=0){lives=3;score=0}}} }
      if(worldx>900&&worldx<900+60&&py==0f){px=worldx-900;vx=0f}; cam=max(0f,px-380f); if(worldx>6370){screen=2;paused=true; if(level==0){level=1;prefs.edit().putInt("level",1).apply();buildLevel()}else{level=0;prefs.edit().putInt("level",0).apply();buildLevel()};px=100f;cam=0f};prefs.edit().putInt("score",score).apply()
    }
    override fun onTouchEvent(e:android.view.MotionEvent):Boolean {val x=e.x*1200/width; val y=e.y*680/height; if(e.action==MotionEvent.ACTION_DOWN){if(screen==0){screen=1;paused=false;return true};if(paused){paused=false;screen=1;return true};left=x<145&&y>480;right=x in 145f..275f&&y>480;run=x>900&&y>480;jump=x>800&&y>480;if(jump&&!jumping){vy=-650f;jumping=true}; if(x>1090&&y<85)paused=true; return true};if(e.action==MotionEvent.ACTION_UP){left=false;right=false;jump=false;run=false};return true }
}
