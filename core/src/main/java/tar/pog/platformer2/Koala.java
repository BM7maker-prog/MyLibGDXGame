package tar.pog.platformer2;

import com.badlogic.gdx.math.Vector2;

public class Koala {
        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 20f;// maximum speed the koala can move.
        static float JUMP_VELOCITY = 80f; //jumping force of koala
        static float DAMPING = 0.87f;//reduces movement speed over time, preventing infinite sliding.

        enum State {
            Standing,
            Walking,
            Jumping,
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;

        public Koala(){

        }

}
