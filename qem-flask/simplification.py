import argparse
import os
from flask import Flask, request
from util.mesh import Mesh

app = Flask(__name__)

def get_parser():
    parser = argparse.ArgumentParser(description="Mesh Simplification")
    parser.add_argument("-i", "--input", type=str, required=True, help="Input file name")
    parser.add_argument("-v", type=int, help="Target vertex number")
    parser.add_argument("-p", type=float, default=0.5, help="Rate of simplification (Ignored by -v)")
    parser.add_argument("-optim", action="store_true", help="Specify for valence aware simplification")
    parser.add_argument("-isotropic", action="store_true", help="Specify for Isotropic simplification")
    args = parser.parse_args()
    return args

def simplify_mesh(args):
    # Handle input path: remove leading '/' and make it relative to current working directory
    args.input = os.path.join(os.getcwd(), args.input.lstrip('/'))
    mesh = Mesh(args.input)
    mesh_name = os.path.basename(args.input).split(".")[-2]
    if args.v:
        target_v = args.v
    else:
        target_v = int(len(mesh.vs) * args.p)
    if target_v >= mesh.vs.shape[0]:
        raise ValueError(f"Target vertex number should be smaller than {mesh.vs.shape[0]}!")
    if args.isotropic:
        simp_mesh = mesh.edge_based_simplification(target_v=target_v, valence_aware=args.optim)
    else:
        simp_mesh = mesh.simplification(target_v=target_v, valence_aware=args.optim)
    os.makedirs("data/output/", exist_ok=True)
    output_file = "data/output/{}_{}.obj".format(mesh_name, simp_mesh.vs.shape[0])
    simp_mesh.save(output_file)
    vertex_count = simp_mesh.vs.shape[0]
    return {
        "message": f"Simplification Completed! Output saved as {output_file}",
        "output_file": output_file,
        "vertex_count": vertex_count
    }

@app.route('/model', methods=['POST'])
def model():
    try:
        data = request.get_json()
        if not data:
            return {"status": "error", "message": "No JSON data provided"}, 400
        
        # Create args object from JSON data
        args = argparse.Namespace()
        args.input = data.get('input')
        if not args.input:
            return {"status": "error", "message": "Missing required 'input' parameter"}, 400
        args.v = data.get('v')
        args.p = data.get('p', 0.5)
        args.optim = data.get('optim', False)
        args.isotropic = data.get('isotropic', False)
        
        result = simplify_mesh(args)
        return {
            "status": "success",
            "message": result["message"],
            "output_file": result["output_file"],
            "vertex_count": result["vertex_count"]
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}, 500

def main():
    args = get_parser()
    try:
        result = simplify_mesh(args)
        print(f"[FIN] {result['message']}")
    except ValueError as e:
        print(f"[ERROR]: {e}")

if __name__ == "__main__":
    app.run(debug=True)